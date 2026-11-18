#!/usr/bin/env python3
"""Serveur maison de Home+, pour le cadre photo (Echo Show, vieille tablette…).

- Sert la page (index.html) et le dossier photos/ à toute la maison.
- http://<serveur>:8000/envoyer : page pour envoyer des photos depuis un iPhone (ou n'importe quel téléphone),
  et les supprimer. Le cadre les affiche au prochain rafraîchissement (2 min environ).

Lancer depuis la racine du dépôt :  python3 tools/serveur.py            (port 8000)
                                    python3 tools/serveur.py --port 8080
Option : PRISME_CODE=1234 python3 tools/serveur.py  → l'envoi et la suppression demandent ce code.

À n'utiliser que sur le réseau de la maison : il n'y a pas d'HTTPS. Python 3.8+, aucune dépendance.
"""
import argparse, email.parser, email.policy, json, os, re, secrets, sys, time
from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler
from urllib.parse import urlparse, parse_qs, unquote

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PHOTOS = os.path.join(RACINE, "photos")
PAGE_ENVOI = os.path.join(RACINE, "tools", "envoyer.html")
CODE = os.environ.get("PRISME_CODE", "")
EXT = (".jpg", ".jpeg", ".png", ".webp", ".gif")
MAX_OCTETS = 40 * 1024 * 1024


def liste_photos():
    try:
        noms = [n for n in os.listdir(PHOTOS) if n.lower().endswith(EXT) and not n.startswith(".")]
    except FileNotFoundError:
        return []
    return sorted(noms, key=lambda n: os.path.getmtime(os.path.join(PHOTOS, n)), reverse=True)


def nom_sur(nom):
    base, ext = os.path.splitext(os.path.basename(nom))
    ext = ext.lower() if ext.lower() in EXT else ".jpg"
    base = re.sub(r"[^A-Za-z0-9_-]+", "-", base).strip("-")[:40] or "photo"
    return f"{time.strftime('%Y%m%d-%H%M%S')}-{secrets.token_hex(2)}-{base}{ext}"


class Gestion(SimpleHTTPRequestHandler):
    def __init__(self, *a, **k):
        super().__init__(*a, directory=RACINE, **k)

    def end_headers(self):
        # Le cadre relit photos/ régulièrement : pas de cache sur les listes.
        if self.path.startswith("/photos/photos.json") or self.path.rstrip("/") in ("/photos", "/envoyer"):
            self.send_header("Cache-Control", "no-store")
        super().end_headers()

    def envoyer_json(self, code, objet):
        corps = json.dumps(objet).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(corps)))
        self.end_headers()
        self.wfile.write(corps)

    def code_ok(self, q):
        return not CODE or q.get("code", [""])[0] == CODE or self.headers.get("X-Prisme-Code", "") == CODE

    def do_GET(self):
        u = urlparse(self.path)
        if u.path.rstrip("/") == "/envoyer":
            with open(PAGE_ENVOI, "rb") as f:
                corps = f.read()
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(corps)))
            self.end_headers()
            self.wfile.write(corps)
            return
        if u.path == "/photos/photos.json":
            self.envoyer_json(200, liste_photos())
            return
        if u.path.startswith("/.git") or u.path.startswith("/tools") or u.path.startswith("/android"):
            self.send_error(404)
            return
        super().do_GET()

    def do_POST(self):
        u = urlparse(self.path)
        q = parse_qs(u.query)
        if not self.code_ok(q):
            self.envoyer_json(403, {"erreur": "Code incorrect"})
            return
        longueur = int(self.headers.get("Content-Length", "0") or 0)
        if longueur > MAX_OCTETS:
            self.envoyer_json(413, {"erreur": "Fichier trop gros"})
            return
        if u.path == "/envoyer":
            brut = self.rfile.read(longueur)
            entete = f"Content-Type: {self.headers.get('Content-Type', '')}\r\n\r\n".encode()
            msg = email.parser.BytesParser(policy=email.policy.default).parsebytes(entete + brut)
            os.makedirs(PHOTOS, exist_ok=True)
            enregistres = []
            for part in msg.iter_parts():
                nom = part.get_filename()
                donnees = part.get_payload(decode=True)
                if not nom or not donnees:
                    continue
                if not (donnees[:3] == b"\xff\xd8\xff" or donnees[:8] == b"\x89PNG\r\n\x1a\n" or donnees[:4] == b"RIFF" or donnees[:3] == b"GIF"):
                    continue  # seulement JPEG, PNG, WebP, GIF (la page d'envoi convertit les photos iPhone en JPEG)
                final = nom_sur(nom)
                with open(os.path.join(PHOTOS, final), "wb") as f:
                    f.write(donnees)
                enregistres.append(final)
            self.envoyer_json(200 if enregistres else 400, {"enregistres": enregistres})
            return
        if u.path == "/supprimer":
            self.rfile.read(longueur)
            nom = os.path.basename(unquote(q.get("nom", [""])[0]))
            chemin = os.path.join(PHOTOS, nom)
            if nom in liste_photos():
                os.remove(chemin)
                self.envoyer_json(200, {"supprime": nom})
            else:
                self.envoyer_json(404, {"erreur": "Introuvable"})
            return
        self.send_error(404)


def main():
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--port", type=int, default=8000)
    a = ap.parse_args()
    os.makedirs(PHOTOS, exist_ok=True)
    srv = ThreadingHTTPServer(("0.0.0.0", a.port), Gestion)
    print(f"Home+ : cadre sur http://<ce-serveur>:{a.port}/?cadre  ·  envoi de photos sur http://<ce-serveur>:{a.port}/envoyer")
    if CODE:
        print("Code demandé pour envoyer ou supprimer des photos.")
    try:
        srv.serve_forever()
    except KeyboardInterrupt:
        pass


if __name__ == "__main__":
    sys.exit(main())

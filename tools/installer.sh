#!/usr/bin/env bash
# Installe (ou met à jour) le serveur photo de Home+ sur un serveur Ubuntu, et le lance au démarrage.
#   curl -fsSL https://raw.githubusercontent.com/Makaveli63/prisme/main/tools/installer.sh | bash
# Relancer la même commande met Home+ à jour. Les photos (dossier photos/) sont conservées.
set -euo pipefail

DOSSIER="${HOMEPLUS_DIR:-$HOME/homeplus}"
PORT="${HOMEPLUS_PORT:-8000}"
DEPOT="https://github.com/Makaveli63/prisme.git"

echo "== Home+ : installation dans $DOSSIER"
command -v git >/dev/null || { echo "Installation de git…"; sudo apt-get update -qq && sudo apt-get install -y -qq git; }
command -v python3 >/dev/null || { echo "Installation de python3…"; sudo apt-get update -qq && sudo apt-get install -y -qq python3; }

if [ -d "$DOSSIER/.git" ]; then
  git -C "$DOSSIER" pull -q --ff-only
else
  git clone -q --depth 1 "$DEPOT" "$DOSSIER"
fi
mkdir -p "$DOSSIER/photos"

if command -v systemctl >/dev/null && [ -d /run/systemd/system ]; then
  sudo tee /etc/systemd/system/homeplus.service >/dev/null <<UNIT
[Unit]
Description=Home+ - cadre photo et envoi de photos
After=network-online.target

[Service]
User=$(id -un)
WorkingDirectory=$DOSSIER
ExecStart=$(command -v python3) $DOSSIER/tools/serveur.py --port $PORT
Restart=always

[Install]
WantedBy=multi-user.target
UNIT
  sudo systemctl daemon-reload
  sudo systemctl enable -q homeplus
  sudo systemctl restart homeplus
  echo "Service « homeplus » démarré (il redémarrera tout seul avec le serveur)."
else
  pkill -f "tools/serveur.py --port $PORT" 2>/dev/null || true
  nohup python3 "$DOSSIER/tools/serveur.py" --port "$PORT" >"$DOSSIER/serveur.log" 2>&1 &
  echo "Serveur lancé en arrière-plan (sans systemd : à relancer après un redémarrage)."
fi

if command -v ufw >/dev/null && sudo ufw status 2>/dev/null | grep -q "Status: active"; then
  sudo ufw allow "$PORT"/tcp >/dev/null && echo "Port $PORT ouvert dans le pare-feu."
fi

IP="$(hostname -I 2>/dev/null | awk '{print $1}')"
IP="${IP:-<adresse-du-serveur>}"
cat <<FIN

== C'est prêt !
  Sur l'Echo Show (navigateur Silk) :  http://$IP:$PORT/
  Sur l'iPhone (Safari), pour envoyer des photos :  http://$IP:$PORT/envoyer

FIN

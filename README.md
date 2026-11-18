# Home+

Cadre photo en verre liquide pour un écran posé dans le salon (Echo Show, vieille tablette…) : une très grande horloge lisible de loin, la date et la météo, par-dessus un diaporama de photos. Publié sur https://makaveli63.github.io/prisme/.

- Heure en très grand, en bas à gauche ou au centre ; date ; météo réelle (Open-Meteo, sans clé) de la ville choisie
- Diaporama en fondu avec léger zoom, une photo toutes les 10 s à 5 min
- Capsule en verre liquide derrière l'heure (réglable de transparent à givré, ou désactivée)
- Assombri la nuit (22 h – 7 h), légère dérive pour éviter le marquage de l'écran, mode léger pour les appareils peu puissants
- Toucher l'écran fait apparaître « Ajouter des photos » et « Réglages »

## Photos

- **Depuis l'appareil** : toucher l'écran, « Ajouter des photos ». Elles restent sur l'appareil.
- **Depuis l'iPhone, via le serveur de la maison** (pour un Echo Show, qui n'a pas de galerie) :
  1. Sur le serveur Ubuntu : `git clone https://github.com/Makaveli63/prisme.git && cd prisme && python3 tools/serveur.py`
  2. Sur l'écran (navigateur Silk de l'Echo Show) : ouvrir `http://<adresse-du-serveur>:8000/`
  3. Sur l'iPhone (Safari) : ouvrir `http://<adresse-du-serveur>:8000/envoyer` et choisir des photos (Partager › « Sur l'écran d'accueil » pour en faire une icône). Elles sont réduites en JPEG avant l'envoi et apparaissent en 2 minutes ; on peut aussi les retirer depuis cette page.
  4. Démarrage automatique : `tools/prisme-cadre.service` (instructions dedans). Option : `PRISME_CODE=1234` pour exiger un code à l'envoi.

  Le serveur n'a pas d'HTTPS : à garder sur le réseau de la maison. Le dossier `photos/` n'est jamais envoyé sur GitHub.

## Application Android

Le dossier `android/` contient la même page dans une WebView plein écran, déclarée comme écran d'accueil : la tablette démarre directement sur le cadre. APK : https://github.com/Makaveli63/prisme/releases/download/apk-main/prisme-home.apk (s'installe par-dessus la version précédente). Dans Réglages, le bouton « Réglages Android » permet de revenir à l'écran d'accueil d'origine.

Lancer en local : `python3 tools/serveur.py` (ou `python3 -m http.server`) puis http://localhost:8000.

Les versions précédentes (lecteur RSS Prisme, puis tableau de bord domotique avec dock d'applications) se trouvent dans l'historique git.

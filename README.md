# Home+

Cadre photo en verre liquide pour un écran posé dans le salon (Echo Show, vieille tablette…) : une très grande horloge lisible de loin, la date et la météo, par-dessus un diaporama de photos. Publié sur https://makaveli63.github.io/prisme/.

- Horloge très lisible de loin : 5 styles (épais, fin, arrondi, aiguilles, en mots : « Il est neuf heures et quart du soir »), en bas à gauche ou au centre, secondes au choix, chiffres blancs ou sombres selon la photo
- Date, fête du jour, météo réelle (Open-Meteo, sans clé) et prévisions (« Pluie vers 17 h », 3 jours)
- Famille : rappels à heure fixe (« 🎒 École dans 12 min »), comptes à rebours (« 🎄 Noël dans 37 jours »), anniversaires avec l'âge, réglables depuis l'iPhone via le serveur
- Enfants : lumière dodo (heure en bleu = on dort, en jaune = on peut se lever)
- Diaporama en fondu avec léger zoom, date de chaque photo, albums de saison (Noël, Halloween, saisons)
- Atténuation selon la lumière, nuit noire (juste l'heure en rouge sombre), dérive anti-marquage, mode léger
- Toucher l'écran : plein écran, « Ajouter des photos », « Réglages »

## Photos

- **Depuis l'appareil** : toucher l'écran, « Ajouter des photos ». Elles restent sur l'appareil.
- **Depuis l'iPhone, via le serveur de la maison** (pour un Echo Show, qui n'a pas de galerie). Sur le serveur Ubuntu, une seule commande, qui installe Home+ et le lance à chaque démarrage (la relancer met à jour) :

  ```bash
  curl -fsSL https://raw.githubusercontent.com/Makaveli63/prisme/main/tools/installer.sh | bash
  ```

  Elle affiche deux adresses : celle à ouvrir sur l'Echo Show (navigateur Silk), et celle à ouvrir sur l'iPhone (Safari) pour envoyer des photos (avec choix de l'album) et régler rappels, anniversaires et comptes à rebours. L'adresse d'envoi est aussi rappelée dans les Réglages de Home+. Les photos sont réduites en JPEG avant l'envoi et apparaissent en 2 minutes.

  Le serveur n'a pas d'HTTPS : à garder sur le réseau de la maison. Code facultatif : `PRISME_CODE=1234` dans le service. Les photos restent dans `~/homeplus/photos/` et ne sont jamais envoyées sur GitHub.

## Application Android

Le dossier `android/` contient la même page dans une WebView plein écran, déclarée comme écran d'accueil : la tablette démarre directement sur le cadre. APK : https://github.com/Makaveli63/prisme/releases/download/apk-main/prisme-home.apk (s'installe par-dessus la version précédente). Dans Réglages, le bouton « Réglages Android » permet de revenir à l'écran d'accueil d'origine.

Lancer en local : `python3 tools/serveur.py` (ou `python3 -m http.server`) puis http://localhost:8000.

Les versions précédentes (lecteur RSS Prisme, puis tableau de bord domotique avec dock d'applications) se trouvent dans l'historique git.

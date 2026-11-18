# Prisme Home

Launcher pour tablette inspiré de la vue Maison d'Apple, en style « Liquid Glass ». Publié sur https://makaveli63.github.io/prisme/.

- Horloge, salutation, météo et résumé de la maison
- Cadrans à faire glisser, comme sur Apple HomeView : Maison, Horloge (lisible de loin) et Lecture ; vue simplifiée en grand quand on ne touche plus l'écran
- Scènes (Bienvenue, Cinéma, Bonne nuit, Départ) et pièces
- Tuiles d'appareils : lumières, thermostat, volets, prises, TV, porte. Touche pour allumer ou éteindre, appui long pour régler
- Carte imprimante Bambu Lab A1 et carte de lecture
- Dock d'applications et tiroir « Toutes » (recherche, appui long pour épingler au dock)
- Thème clair / sombre / auto, plein écran, mode veille après 2 minutes (horloge numérique ou analogique, mode nuit automatique de 22 h à 7 h)
- Installable sur l'écran d'accueil (plein écran, paysage)

C'est pour l'instant une **maquette** : les appareils, la météo, l'imprimante et la lecture sont des exemples définis en haut du script de `index.html` (`DEVICES`, `SCENES`, `APPS`, `printer`, `media`). Les applications du dock ne s'ouvrent pas depuis une page web : il faut l'application Android.

## Cadre photo (Echo Show, vieille tablette…)

Ouvrir la page avec `?cadre` à la fin de l'adresse : heure en grand par-dessus un diaporama, sans applications.

- Photos depuis l'appareil : toucher l'écran puis « Ajouter des photos » (elles restent sur l'appareil).
- Photos depuis un serveur de la maison (pratique pour un Echo Show, qui n'a pas de galerie) : sur le serveur, cloner ce dépôt, mettre les photos dans un dossier `photos/` à côté de `index.html`, lancer `python3 -m http.server 8000` dans le dépôt, puis ouvrir `http://<adresse-du-serveur>:8000/?cadre` sur l'Echo Show (navigateur Silk).

## Application Android

Le dossier `android/` contient un launcher Android : la même page dans une WebView plein écran, avec un dock qui lance les vraies applications.

1. Sur la tablette, ouvrir la page **Releases** du dépôt GitHub, pré-version `apk-main` (ou `apk-launcher`), et télécharger `prisme-home.apk`.
2. Autoriser l'installation d'applications depuis le navigateur quand Android le demande, puis installer.
3. Appuyer sur le bouton Accueil et choisir **Prisme Home**, « Toujours ». Sinon : Réglages › Applications › Application d'accueil par défaut (le tiroir « Toutes » propose aussi le raccourci).

Pour revenir au launcher d'origine, changer l'application d'accueil dans les réglages ou désinstaller Prisme Home.

Lancer en local : `python3 -m http.server` puis ouvrir http://localhost:8000.

L'ancien lecteur RSS Prisme se trouve dans l'historique git, avant la branche `launcher`.

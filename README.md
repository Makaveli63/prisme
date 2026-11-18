# Prisme Home

Launcher pour tablette inspiré de la vue Maison d'Apple, en style « Liquid Glass ». Publié sur https://makaveli63.github.io/prisme/.

- Horloge, salutation, météo et résumé de la maison
- Scènes (Bienvenue, Cinéma, Bonne nuit, Départ) et pièces
- Tuiles d'appareils : lumières, thermostat, volets, prises, TV, porte. Touche pour allumer ou éteindre, appui long pour régler
- Carte imprimante Bambu Lab A1 et carte de lecture
- Dock d'applications
- Thème clair / sombre / auto, plein écran, mode veille avec grande horloge après 2 minutes sans toucher l'écran
- Installable sur l'écran d'accueil (plein écran, paysage)

C'est pour l'instant une **maquette** : les appareils, la météo, l'imprimante et la lecture sont des exemples définis en haut du script de `index.html` (`DEVICES`, `SCENES`, `APPS`, `printer`, `media`). Les applications du dock ne s'ouvrent pas depuis une page web.

Lancer en local : `python3 -m http.server` puis ouvrir http://localhost:8000.

L'ancien lecteur RSS Prisme se trouve dans l'historique git, avant la branche `launcher`.

# Prisme Home

Launcher pour tablette inspiré de la vue Maison d'Apple, en style « Liquid Glass ». Publié par GitHub Pages sur https://makaveli63.github.io/prisme/ après fusion dans `main`.

Le dépôt contenait avant un lecteur RSS (Prisme). Il est supprimé de la branche `launcher` mais reste dans l'historique git, avant le commit « Remplace le lecteur RSS par le launcher Prisme Home ».

## Structure

- `index.html` : toute l'application (CSS et JavaScript inclus, aucune dépendance, aucun build).
- `manifest.webmanifest`, `icon.svg`, `icon-180.png`, `icon-512.png` : installation sur l'écran d'accueil (plein écran, paysage).
- `.github/workflows/pages.yml` : copie ces fichiers dans `_site/` et publie sur Pages à chaque push sur `main`.

## Dans `index.html`

- Données d'exemple en haut du script : `USER`, `WEATHER`, `ROOMS`, `DEVICES`, `SCENES`, `APPS`, `printer`, `media`.
- Icônes : objet `ICONS` (tracés SVG), helper `ic(nom)`.
- Rendu par fonctions : `renderScenes`, `renderRooms`, `renderGrid`, `renderStatus`, `renderPrinter`, `renderMedia`.
- Tuiles : un appui bascule l'appareil (`toggle`), un appui long de 450 ms ouvre la fiche de réglage (`openSheet`) pour les lumières, le thermostat et les volets.
- Thème auto / clair / sombre, plein écran, mode veille après `IDLE_MS` sans toucher l'écran.
- Mise en page : grille paysage (colonne principale + colonne latérale + dock) et colonne unique en portrait (`max-aspect-ratio: 1/1`). L'unité de base est `rem`, calée sur la hauteur de l'écran.

## Style

Palette et matière verre repris de l'ancien Prisme : variables `--glass`, `--rim`, `--on-bg`, fond de blobs flous animés. Tuile allumée : fond blanc, texte sombre. Tuile éteinte : verre.

## État actuel

Maquette : tout est local, aucun appareil réel n'est piloté. Les applications du dock affichent seulement un message, une page web ne peut pas lancer d'applications Android.

## Prochaines étapes

1. Brancher les tuiles sur Home Assistant (API REST ou WebSocket). Contrainte : une page servie en HTTPS (GitHub Pages) ne peut pas appeler un Home Assistant en HTTP, il faut HTTPS ou une application native.
2. Faire une application Android (APK) avec une WebView plein écran déclarée comme launcher (`CATEGORY_HOME`), ce qui permet aussi de lancer de vraies applications depuis le dock.
3. Remplacer les données d'exemple par des entités réelles (stockées localement, jamais de jeton dans le dépôt).

## Tests

Pas de suite de tests. Vérification manuelle : `python3 -m http.server` puis http://localhost:8000, en 1280×800 (paysage) et 800×1280 (portrait), en thème clair et sombre.

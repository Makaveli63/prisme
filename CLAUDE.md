# Prisme Home

Launcher pour tablette inspiré de la vue Maison d'Apple, en style « Liquid Glass ». Publié par GitHub Pages sur https://makaveli63.github.io/prisme/ après fusion dans `main`.

Le dépôt contenait avant un lecteur RSS (Prisme). Il est supprimé de la branche `launcher` mais reste dans l'historique git, avant le commit « Remplace le lecteur RSS par le launcher Prisme Home ».

## Structure

- `index.html` : toute l'application (CSS et JavaScript inclus, aucune dépendance, aucun build).
- `manifest.webmanifest`, `icon.svg`, `icon-180.png`, `icon-512.png` : installation sur l'écran d'accueil (plein écran, paysage).
- `.github/workflows/pages.yml` : copie ces fichiers dans `_site/` et publie sur Pages à chaque push sur `main`.
- `android/` : application Android (Java, sans AppCompat, seule dépendance `androidx.webkit`). `MainActivity` affiche `index.html` dans une WebView plein écran et se déclare launcher (`CATEGORY_HOME`). La tâche Gradle `copyWeb` copie `index.html` et les icônes de la racine dans les assets : la page n'existe qu'une fois.
- `android/debug.keystore` : clé de debug fixe (mot de passe public `android`), pour que chaque APK s'installe par-dessus le précédent. Ce n'est pas une clé de publication.
- `.github/workflows/android.yml` : compile l'APK à chaque push sur `main` ou `launcher` et le publie en pré-version GitHub `apk-<branche>` (fichier `prisme-home.apk`).

## Dans `index.html`

- Données d'exemple en haut du script : `USER`, `WEATHER`, `ROOMS`, `DEVICES`, `SCENES`, `APPS`, `printer`, `media`.
- Icônes : objet `ICONS` (tracés SVG), helper `ic(nom)`.
- Rendu par fonctions : `renderScenes`, `renderRooms`, `renderGrid`, `renderStatus`, `renderPrinter`, `renderMedia`.
- Tuiles : un appui bascule l'appareil (`toggle`), un appui long de 450 ms ouvre la fiche de réglage (`openSheet`) pour les lumières, le thermostat et les volets.
- Thème auto / clair / sombre, plein écran.
- Mode veille après `IDLE_MS` sans toucher l'écran : horloge numérique géante ou analogique en verre (bouton pour changer, choix gardé dans `localStorage`), date, météo et avancement de l'impression, légère dérive anti-marquage, assombrissement chaud de 22 h à 7 h (classe `night`). Fonctions : `ambientTick`, `analogLoop`, `buildAnalog`.
- Applications : `APPS` est le dock par défaut (`pkg` = paquet Android, `android:settings` = réglages). Le dock modifié est stocké dans localStorage (`prisme-dock`). Bouton « Toutes » : tiroir avec recherche ; appui court lance, appui long épingle ou retire du dock.
- Pont natif : `window.PrismeAndroid` (absent dans un navigateur, tout reste alors en aperçu) avec `listApps`, `appIcon`, `launchApp`, `openStore`, `openSettings`, `openAppInfo`, `isDefaultLauncher`, `version`. L'activité envoie les événements `prisme-back`, `prisme-home` et `prisme-resume` à la page.
- Mise en page : grille paysage (colonne principale + colonne latérale + dock) et colonne unique en portrait (`max-aspect-ratio: 1/1`). L'unité de base est `rem`, calée sur la hauteur de l'écran.

## Style

Palette et matière verre repris de l'ancien Prisme : variables `--glass`, `--rim`, `--on-bg`, fond de blobs flous animés. Tuile allumée : fond blanc, texte sombre. Tuile éteinte : verre.

## État actuel

Maquette pour la maison : tout est local, aucun appareil réel n'est piloté. Dans l'APK, le dock et le tiroir lancent les vraies applications installées ; dans un navigateur, ils affichent seulement un message.

La page est servie dans l'APK depuis `https://appassets.androidplatform.net/assets/` (WebViewAssetLoader), avec le contenu mixte autorisé : elle pourra appeler un Home Assistant local en HTTP.

## Prochaines étapes

1. Brancher les tuiles sur Home Assistant (API REST ou WebSocket). Depuis GitHub Pages il faut un Home Assistant en HTTPS ; depuis l'APK le HTTP local passe (ajouter l'origine `https://appassets.androidplatform.net` à `cors_allowed_origins`).
2. Remplacer les données d'exemple par des entités réelles (stockées localement, jamais de jeton dans le dépôt).

## Tests

Pas de suite de tests. Vérification manuelle : `python3 -m http.server` puis http://localhost:8000, en 1280×800 (paysage) et 800×1280 (portrait), en thème clair et sombre.

Le conteneur Claude Code n'a pas de SDK Android (dl.google.com bloqué) : l'APK se compile seulement dans le workflow `android.yml`. En local avec un SDK : `cd android && ./gradlew assembleDebug`.

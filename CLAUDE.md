# Home+

Cadre photo en verre liquide : très grande horloge lisible de loin, date et météo, sur un diaporama de photos. Pensé pour un Echo Show 8 posé sous la TV. Publié par GitHub Pages sur https://makaveli63.github.io/prisme/ après fusion dans `main`.

Nom : **Home+**. Le dépôt, l'adresse GitHub Pages, le paquet Android `io.github.makaveli63.prisme` et le fichier `prisme-home.apk` gardent l'ancien nom (Prisme) pour ne pas casser les liens ni les mises à jour de l'APK. Ne pas utiliser « HomeView » seul (nom d'un produit Apple).

L'utilisateur ne veut **que** l'horloge, la date, la météo et les photos : pas d'applications, de dock, de domotique ni de cartes. Les versions précédentes (lecteur RSS, puis tableau de bord domotique avec cadrans façon HomeView, dock et tiroir d'applications) sont dans l'historique git, jusqu'au commit « Renomme l'application en Home+ » inclus.

## Structure

- `index.html` : toute l'application (CSS et JavaScript inclus, aucune dépendance, aucun build).
- `manifest.webmanifest`, `icon.svg`, `icon-180.png`, `icon-512.png` : installation sur l'écran d'accueil. Icône : maison en verre liquide traversée par un faisceau qui se décompose en arc-en-ciel ; `icon.svg` est la source, les PNG (et l'icône adaptative Android dans `android/app/src/main/res/`) sont rendus depuis le SVG avec Chromium.
- `tools/serveur.py` (Python, sans dépendance) : serveur de la maison. Sert la page et `photos/`, `photos/photos.json` (liste), `/envoyer` (page `tools/envoyer.html` pour envoyer depuis un iPhone, réduction en JPEG 2000 px dans le navigateur) et `POST /envoyer`, `POST /supprimer?nom=`. Code facultatif `PRISME_CODE`. `tools/prisme-cadre.service` : démarrage systemd.
- `android/` : application Android (Java, seule dépendance `androidx.webkit`). `MainActivity` affiche `index.html` dans une WebView plein écran et se déclare launcher (`CATEGORY_HOME`). La tâche Gradle `copyWeb` copie `index.html` et les icônes dans les assets. Le pont `window.PrismeAndroid` (listApps, launchApp, openSettings…) reste disponible ; la page n'utilise que `openSettings("home")`. Événements envoyés à la page : `prisme-back`, `prisme-home`, `prisme-resume`.
- `android/debug.keystore` : clé de debug fixe (mot de passe public `android`), pour que chaque APK s'installe par-dessus le précédent.
- `.github/workflows/pages.yml` : publie sur Pages à chaque push sur `main`. `.github/workflows/android.yml` : compile l'APK sur `main` et `launcher` et le publie en pré-version `apk-<branche>`.

## Dans `index.html`

- Réglages dans localStorage `homeplus` (objet `cfg`) : `city` {name, lat, lon}, `pos` (`corner` ou `center`), `every` (secondes par photo), `plate` (capsule de verre derrière l'heure), `glass` (0–100, transparent ↔ givré), `night` (assombrir 22 h – 7 h), `lite` (mode léger, activé d'office si `deviceMemory` ≤ 2 Go ou ≤ 2 cœurs). `apply()` les applique.
- Heure et date : `tick()` chaque seconde. Taille calée sur la hauteur de l'écran (`vh`) pour être lisible de loin.
- Météo : Open-Meteo (`loadWeather`, toutes les 30 min, dernière météo gardée 6 h dans `homeplus-wx` en cas de coupure), ville cherchée par le géocodage Open-Meteo (`setCity`). Icônes `WX_ICONS`, libellés `wxKind(code, jour)`.
- Photos : objet `Photos`. Sources cumulées : photos ajoutées depuis l'appareil (IndexedDB `prisme-photos`, jamais envoyées) et dossier `photos/` servi à côté de la page (liste du serveur ou `photos/photos.json`), ou `?photos=<adresse>` ; relu toutes les 2 min. Sans photo, des dégradés servent d'exemple. `photos/` est dans `.gitignore` : ne jamais committer de photos.
- Verre liquide : classe `.glass` (flou, liseré spéculaire `::after` orienté par `--lx`/`--ly`), réfraction des bords par l'objet `LiquidGlass` (carte de déplacement dessinée dans un canvas, filtre SVG en `backdrop-filter`, Chromium 111+ seulement, coupée en mode léger). Repli `@supports not (color-mix)` pour les vieilles WebView.
- Plein écran demandé au premier toucher (`goFullscreen`, cache la barre de Silk sur l'Echo Show ; ignoré dans l'APK) et écran gardé allumé par Wake Lock (`keepAwake`).
- Toucher l'écran affiche « Ajouter des photos » et « Réglages » pendant 7 s.

## Tests

Pas de suite de tests. Vérification manuelle : `python3 tools/serveur.py` puis http://localhost:8000, en 1280×800 (Echo Show 8, paysage) et 800×1280, avec et sans photos dans `photos/`. Le conteneur Claude Code n'a accès ni à Open-Meteo ni au SDK Android : simuler les réponses météo (Playwright `route`) et laisser le workflow `android.yml` compiler l'APK.

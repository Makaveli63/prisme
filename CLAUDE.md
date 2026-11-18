# Home+

Cadre photo en verre liquide : très grande horloge lisible de loin, date et météo, sur un diaporama de photos. Pensé pour un Echo Show 8 posé sous la TV. Publié par GitHub Pages sur https://makaveli63.github.io/prisme/ après fusion dans `main`.

Nom : **Home+**. Le dépôt, l'adresse GitHub Pages, le paquet Android `io.github.makaveli63.prisme` et le fichier `prisme-home.apk` gardent l'ancien nom (Prisme) pour ne pas casser les liens ni les mises à jour de l'APK. Ne pas utiliser « HomeView » seul (nom d'un produit Apple).

L'utilisateur veut un cadre horloge + photos : pas d'applications, de dock, de domotique, ni de cadre en verre derrière l'heure. Ajouts voulus : styles d'horloge, couleur auto, secondes, prévisions, rappels, comptes à rebours, fête du jour et anniversaires, lumière dodo, heure en mots, date des photos, albums de saison, nuit noire. Les versions précédentes (lecteur RSS, puis tableau de bord domotique avec cadrans façon HomeView, dock et tiroir d'applications) sont dans l'historique git, jusqu'au commit « Renomme l'application en Home+ » inclus.

## Structure

- `index.html` : toute l'application (CSS et JavaScript inclus, aucune dépendance, aucun build).
- `manifest.webmanifest`, `icon.svg`, `icon-180.png`, `icon-512.png` : installation sur l'écran d'accueil. Icône : maison en verre liquide traversée par un faisceau qui se décompose en arc-en-ciel ; `icon.svg` est la source, les PNG (et l'icône adaptative Android dans `android/app/src/main/res/`) sont rendus depuis le SVG avec Chromium.
- `tools/serveur.py` (Python, sans dépendance) : serveur de la maison. Sert la page et `photos/`, `photos/photos.json` (liste, albums compris : `noel/xxx.jpg`), `/envoyer` (page `tools/envoyer.html` : album, date EXIF lue avant la réduction en JPEG 2000 px, lien vers `/?reglages`), `POST /envoyer?album=&prise=AAAAMMJJ`, `POST /supprimer?nom=`, `GET`/`POST /donnees.json` (données Famille, rangées dans `photos/donnees.json`, non servies directement). Code facultatif `PRISME_CODE`. `tools/prisme-cadre.service` : démarrage systemd.
- `tools/installer.sh` : installation en une commande sur le serveur Ubuntu (`curl … | bash`) : clone ou met à jour dans `~/homeplus`, service systemd `homeplus` (ou `nohup` sans systemd), ouvre le port dans ufw, affiche les adresses. Quand la page est servie par `serveur.py`, les Réglages affichent l'adresse `/envoyer`.
- `android/` : application Android (Java, seule dépendance `androidx.webkit`). `MainActivity` affiche `index.html` dans une WebView plein écran et se déclare launcher (`CATEGORY_HOME`). La tâche Gradle `copyWeb` copie `index.html` et les icônes dans les assets. Le pont `window.PrismeAndroid` (listApps, launchApp, openSettings…) reste disponible ; la page n'utilise que `openSettings("home")`. Événements envoyés à la page : `prisme-back`, `prisme-home`, `prisme-resume`.
- `android/debug.keystore` : clé de debug fixe (mot de passe public `android`), pour que chaque APK s'installe par-dessus le précédent.
- `.github/workflows/pages.yml` : publie sur Pages à chaque push sur `main`. `.github/workflows/android.yml` : compile l'APK sur `main` et `launcher` et le publie en pré-version `apk-<branche>`.

## Dans `index.html`

- Pas de cadre ni de verre derrière l'heure (retiré à la demande) : le texte flotte sur la photo, avec une ombre.
- Réglages de l'écran dans localStorage `homeplus` (objet `cfg`, appliqués par `apply()`) : `city`, `style` (`bold`, `thin`, `rounded`, `analog`, `words`), `pos` (`corner`, `center`), `sec` (`off`, `on`, `blink`), `ink` (`auto` = chiffres sombres sur photo claire, `white`), `forecast`, `fete`, `every`, `caption`, `night` + `dimMax`, `deep` + `deepFrom`/`deepTo`, `kid` + `kidBed`/`kidWake`, `lite`. `?reglages` dans l'adresse ouvre les réglages.
- Horloge : `tick()` chaque seconde (le reste à la minute via `lastMin`). `inWords()` donne l'heure en mots (arrondie à 5 min). Cadran à aiguilles en SVG (`#analog`, aiguilles `#hh`, `#hm`, `#hs`), à gauche des informations.
- Famille (objet `fam` : `reminders` [{t, x, d: all|week|weekend}], `events` [{d, x, y}] (`y` = chaque année, `d` = `MM-DD` ; sinon `AAAA-MM-JJ`), `birthdays` [{d: date de naissance, x}]). Partagé par le serveur (`donnees.json`, `SERVER`) quand la page vient de lui, sinon localStorage `homeplus-fam`. Rappels : bandeau 15 min avant puis 5 min (`reminderNow`). Comptes à rebours et anniversaires : 3 pastilles au plus (`upcoming`), anniversaires 30 jours avant.
- Fête du jour : tableau `FETES` (calendrier usuel, « ! » = jour férié ou fête affichée telle quelle), source dans l'historique (script Python de génération non versionné).
- Lumière dodo : classes `kid-sleep` (bleu) entre coucher et lever, `kid-wake` (jaune) pendant l'heure qui suit le lever. Nuit noire : classe `deep` (photos masquées, heure seule en rouge sombre).
- Météo et prévisions : Open-Meteo (`loadWeather`, 30 min) : actuel, 12 h à venir (« Pluie vers 17 h »), 3 jours, lever / coucher du soleil. Dernière météo gardée 6 h (`homeplus-wx`). Ville via le géocodage Open-Meteo (`setCity`).
- Atténuation : `dimLevel(now)` → `--dim` (voile chaud `.dim`, texte atténué). `AmbientLightSensor` si disponible, sinon soleil.
- Photos : objet `Photos`. Photos de l'appareil (IndexedDB `prisme-photos`) + `photos/photos.json` du serveur (ou `?photos=`), relu toutes les 2 min. Albums de saison `photos/<album>/` filtrés par `SEASONS`. Date de la photo lue dans le nom `d<AAAAMMJJ>-…` (légende « Juillet 2023 »). Couleur des chiffres : clarté moyenne de la zone sous l'heure (`ink`, classe `ink-dark`).
- Plein écran au premier toucher (`goFullscreen`, marche dans Silk sur l'Echo Show), écran gardé allumé (`keepAwake`, Wake Lock). Toucher l'écran affiche « Ajouter des photos » et « Réglages » pendant 7 s.

## Tests

Pas de suite de tests. Vérification manuelle : `python3 tools/serveur.py` puis http://localhost:8000, en 1280×800 (Echo Show 8, paysage) et 800×1280, avec et sans photos dans `photos/`. Le conteneur Claude Code n'a accès ni à Open-Meteo ni au SDK Android : simuler les réponses météo (Playwright `route`) et laisser le workflow `android.yml` compiler l'APK.

# Prisme Home

Launcher pour tablette inspiré de la vue Maison d'Apple, en style « Liquid Glass ». Publié par GitHub Pages sur https://makaveli63.github.io/prisme/ après fusion dans `main`.

Le dépôt contenait avant un lecteur RSS (Prisme). Il est supprimé de la branche `launcher` mais reste dans l'historique git, avant le commit « Remplace le lecteur RSS par le launcher Prisme Home ».

## Structure

- `index.html` : toute l'application (CSS et JavaScript inclus, aucune dépendance, aucun build).
- `manifest.webmanifest`, `icon.svg`, `icon-180.png`, `icon-512.png` : installation sur l'écran d'accueil (plein écran, paysage).
- `.github/workflows/pages.yml` : copie ces fichiers dans `_site/` et publie sur Pages à chaque push sur `main`.
- `android/` : application Android (Java, sans AppCompat, seule dépendance `androidx.webkit`). `MainActivity` affiche `index.html` dans une WebView plein écran et se déclare launcher (`CATEGORY_HOME`). La tâche Gradle `copyWeb` copie `index.html` et les icônes de la racine dans les assets : la page n'existe qu'une fois.
- Icône : maison en verre liquide traversée par un faisceau qui se décompose en arc-en-ciel (le prisme), sur fond indigo → violet → magenta. `icon.svg` est la source ; les PNG (`icon-180.png`, `icon-512.png`, et dans `android/app/src/main/res/` l'icône adaptative `ic_launcher_foreground`/`ic_launcher_background` plus `ic_launcher.png` arrondi) sont rendus depuis le SVG avec Chromium.
- `android/debug.keystore` : clé de debug fixe (mot de passe public `android`), pour que chaque APK s'installe par-dessus le précédent. Ce n'est pas une clé de publication.
- `tools/serveur.py` (Python, sans dépendance) : serveur de la maison pour le cadre photo. Sert la page et `photos/`, `photos/photos.json` (liste), `/envoyer` (page `tools/envoyer.html` pour envoyer depuis un iPhone, réduction en JPEG 2000 px dans le navigateur) et `POST /envoyer`, `POST /supprimer?nom=`. Code facultatif `PRISME_CODE`. `tools/prisme-cadre.service` : démarrage systemd. Le cadre relit les photos toutes les 2 min.
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
- Cadrans façon Apple HomeView (`#faces`, défilement horizontal aimanté, points `#dots`, dernier cadran gardé dans localStorage `prisme-face`) : Maison (`#face-home`, l'écran domotique), Horloge (`#face-clock`, grande heure, météo, complications `renderComplications`) et Lecture (`#face-play`). Accueil et Retour reviennent au cadran Maison (`goFace(0)`).
- Cadran Photos (`#face-photo`, objet `Photos`) : diaporama plein écran (fondu + effet Ken Burns, `PHOTO_MS`) avec l'heure dans une capsule de verre, assombri de 22 h à 7 h. Sources cumulées : photos ajoutées depuis l'appareil (réduites, IndexedDB `prisme-photos`, jamais envoyées) et dossier `photos/` servi à côté de la page (liste du serveur ou `photos/photos.json`), ou `?photos=<adresse>`. `photos/` est dans `.gitignore` : ne jamais committer de photos.
- Mode cadre photo (classe `cadre`, `?cadre` dans l'adresse ou interrupteur dans Apparence) : seul le cadran Photos, sans dock, points ni veille. Pensé pour l'Echo Show sous la TV.
- Vue de loin / de près : après `FAR_MS` sans toucher l'écran, classe `far` sur `<html>` (textes agrandis, détails, dock et boutons masqués) ; le premier toucher revient à la vue de près sans rien déclencher (`nearNow`). HomeView mesure la distance réelle, ici l'inactivité sert d'approximation.
- Mise en page : cadrans + points + dock ; le cadran Maison est une grille paysage (colonne principale + colonne latérale) et colonne unique en portrait (`max-aspect-ratio: 1/1`). L'unité de base est `rem`, calée sur la hauteur de l'écran.

## Style

Liquid Glass façon iOS 26/27. Fond : dégradé `--wall` + blobs flous animés. Verre (classe `.glass`) : remplissage `--g-fill`, flou `--g-blur`, liseré spéculaire `::after` dont l'angle suit l'inclinaison de la tablette ou la souris (`--lx`, `--ly`), lumière sous le doigt `::before` (`.pressing`, `--mx`, `--my`), gonflement à ressort (`--spring`).

- Réfraction : objet `LiquidGlass` dans le script. Pour chaque taille de verre il dessine une carte de déplacement (canvas, forme arrondie) et l'applique par un filtre SVG dans `backdrop-filter` (`--lg-f`). Chromium seulement (Chrome, WebView Android) ; ailleurs le verre reste flou. Désactivée si `prefers-reduced-transparency`.
- Bouton Apparence (`#look`, réglages dans localStorage `prisme-look`) : thème, intensité du verre de transparent à givré (`--g-v`, qui règle aussi la force de la réfraction), verre teinté (classe `tinted` sur `<html>`) et couleur d'accent (`--tint`).
- Mode léger (`lite` sur `<html>`, interrupteur dans Apparence, activé d'office si `deviceMemory` ≤ 2 Go ou ≤ 2 cœurs) : fond fixe, flou réduit, pas de réfraction. Pensé pour l'Echo Show 8 de 1re génération sous LineageOS 18.1.
- Vieilles WebView (avant Chromium 111, sans `color-mix()` ni `atan2()`) : bloc `@supports not` avec des couleurs fixes ; la réfraction exige Chromium 111.
- Tuile allumée : verre blanc dense, texte sombre. Tuile éteinte : verre clair.

## État actuel

Maquette pour la maison : tout est local, aucun appareil réel n'est piloté. Dans l'APK, le dock et le tiroir lancent les vraies applications installées ; dans un navigateur, ils affichent seulement un message.

La page est servie dans l'APK depuis `https://appassets.androidplatform.net/assets/` (WebViewAssetLoader), avec le contenu mixte autorisé : elle pourra appeler un Home Assistant local en HTTP.

## Prochaines étapes

1. Brancher les tuiles sur Home Assistant (API REST ou WebSocket). Depuis GitHub Pages il faut un Home Assistant en HTTPS ; depuis l'APK le HTTP local passe (ajouter l'origine `https://appassets.androidplatform.net` à `cors_allowed_origins`).
2. Remplacer les données d'exemple par des entités réelles (stockées localement, jamais de jeton dans le dépôt).

## Tests

Pas de suite de tests. Vérification manuelle : `python3 -m http.server` puis http://localhost:8000, en 1280×800 (paysage) et 800×1280 (portrait), en thème clair et sombre.

Le conteneur Claude Code n'a pas de SDK Android (dl.google.com bloqué) : l'APK se compile seulement dans le workflow `android.yml`. En local avec un SDK : `cd android && ./gradlew assembleDebug`.

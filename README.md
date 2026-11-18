# Flux — lecteur RSS (webapp)

Lecteur de flux RSS/Atom inspiré des apps type Feedler, dans le dossier [`flux/`](flux/).

- Abonnements RSS/Atom, détection automatique du flux depuis l'adresse d'un site
- Dossiers, compteurs de non lus, vues « Tous / Non lus / Aujourd'hui / Favoris »
- Lecteur d'article intégré (police serif/sans), partage, ouverture de l'original
- Recherche, masquer les articles lus, tout marquer comme lu
- Import / export OPML, thème clair/sombre/auto
- Données stockées localement (IndexedDB), installable sur iPhone (PWA)
- Raccourcis clavier : j/k, s, m, o, r, /

Lancer en local : `cd flux && python3 -m http.server` puis ouvrir http://localhost:8000.

Les flux sont téléchargés via des proxys CORS publics (allorigins, corsproxy.io, codetabs) quand le site ne permet pas l'accès direct.

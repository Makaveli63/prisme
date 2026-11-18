# Prisme — lecteur RSS

Webapp de lecture de flux RSS/Atom au style « Liquid Glass », accessible sur https://makaveli63.github.io/prisme/.

- Sources par catégories (Apple, Gaming, Tech…) avec icônes, compteurs, repli
- Vues : Tous les articles · Articles du jour · À la une
- Filtre flottant : Favoris · Non lus · Tout
- Lecteur intégré (serif / sans), partage, ouverture dans Safari, article suivant / précédent
- Ajout d'une source par simple adresse de site (détection automatique du flux)
- Import / export OPML, thème clair / sombre / auto, tirer pour actualiser, glisser depuis le bord pour revenir
- Données stockées localement (IndexedDB), installable sur l'écran d'accueil de l'iPhone

Lancer en local : `python3 -m http.server` puis ouvrir http://localhost:8000.

Les flux sont téléchargés via des proxys CORS publics (allorigins, corsproxy.io, codetabs) quand le site ne permet pas l'accès direct.

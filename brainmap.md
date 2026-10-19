# OptiMem — carte technique 0.1.0

## Éléments et propagation
- app/build.gradle : namespace/applicationId fr.fab.optimem ; compileSdk/targetSdk 36 ; Java 17 ; version 0.1.0. Changer package requiert révision manifeste/source/identité.
- app/src/main/AndroidManifest.xml : MainActivity unique exportée (lanceur), permission spéciale PACKAGE_USAGE_STATS (octroi dans les réglages), visibilité restreinte au catalogue des apps avec launcher ; pas d'INTERNET, service, receiver, provider ou droit de suppression.
- app/src/main/res/values/styles.xml : thème natif sombre accessible, sans bibliothèque UI supplémentaire.
- app/src/main/res/drawable/ic_optichip.xml : icône vectorielle puce souriante avec balai.
- app/src/main/java/fr/fab/optimem/MainActivity.java : création UI native, cycle onResume/onPause, lecture MemoryInfo, échantillonnage pendant visibilité, courbe, avant/après en SharedPreferences, guide manuel, dialogue usage, sélecteur lanceur, navigation vers settings, copie journal, suppression cache privé.
- app/src/main/java/fr/fab/optimem/MemoryChart.java : tracé léger des seuls échantillons réellement mesurés.
- app/src/main/java/fr/fab/optimem/LocalJournal.java : fichier interne local et borné, append horodaté, copie manuelle via interface, pas d'envoi réseau.
- .github/workflows/build-android.yml : push branche dédiée -> SDK 36 / Gradle Java 17 -> assembleDebug -> copier APK directement sous temp/ et produire SHA-256 -> commit robot sur même branche (push paths-ignore temp évite boucle). Ne fusionne pas main, ne publie aucune Release. Le workflow CI doit faire preuve du véritable APK avant de déclarer livraison.
- ordres-de-mission.md : mandat immuable et complet ; brain.md : contrat ; brainmap.md : carte ; debughistorical.md : état anomalies preuves ; todo.md : plan et blocages.

## Flux
MainActivity.onResume -> Handler 4s -> ActivityManager.getMemoryInfo -> historique borné -> labels et MemoryChart. onPause -> stop Handler, donc aucun service permanent.
Préparer -> snapshot avant enregistré -> conseils/Ouvrir réglages -> retour utilisateur -> mesure après -> delta affiché et journal.
Accès usage : vérification AppOps (MODE_ALLOWED) -> queryEvents sur fenêtre 6h -> dernier ACTIVITY_RESUMED ou MOVE_TO_FOREGROUND / package -> liste triée, jamais statut processus confirmé.
Dossier cacheDir -> suppression récursive de fichiers internes uniquement -> nombre d'octets d'objets effectivement supprimés affiché ; n'inclut pas cache d'autres applications.
Journal LocalJournal -> private filesDir -> presse-papiers sur demande ; capacité bornée, pas de permission de stockage.
Applications IA : queryIntentActivities MAIN+LAUNCHER -> choix local dans SharedPreferences -> getLaunchIntentForPackage puis startActivity si disponible.

## Limites explicitement non implémentées
- RAM résidente par application tierce et distinction exacte RAM libre / récupérable (API36).
- Démarrage de Device Care / RAM Plus via composant Samsung non documenté ; seul accès général aux paramètres.
- Signature AAB Play durable (aucune clé d'importation configurée).
- Validation matérielle sur Samsung, indépendante de la compilation GitHub.

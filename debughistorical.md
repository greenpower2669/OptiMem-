# OptiMem — incidents, observations et preuves

## 2026-10-19 — AUDIT-001 : la notion de "nettoyer toute la RAM" est invalide
Observation documentée : ActivityManager.MemoryInfo fournit une mesure globale et Android restreint la visibilité/arrêt des autres processus. Hypothèse initiale à exclure : mesurer individuellement et arrêter arbitrairement les apps tierces sans privilèges.
Résolution de conception : interface ne fait aucun faux nettoyage ; accès aux paramètres manuels et labels "information non disponible". Pas d'incident de fonctionnement constaté sur appareil (tests non exécutés).
Sources : https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo ; https://developer.android.com/reference/android/app/usage/UsageStatsManager

## 2026-10-19 — INFRA-001 : nom du dépôt
La demande mentionne greenpower2669/OptiMem. Le dépôt existant et accessible avec droit push est greenpower2669/OptiMem- (tiret final) ; branche dédiée feature/optimem-v0.1.0-native-memory créée, main conservée. Aucun renommage tenté.

## 2026-10-19 — BUILD-001 : preuves attendues
L'état du code source / la création de branche ne valent pas succès de compilation. Avant d'affirmer la livraison, relever run GitHub Actions, conclusion, commit et SHA-256 effectivement présents, ainsi que le lien .apk existant. Erreurs CI à consigner sans inventer de succès.

## 2026-10-19 — BUILD-002 : action setup-android@v3 incompatible avec SDK du runner
- Run #39243768981, job 118030594881 : FAILURE avant compilation ; setup-android appelle sdkmanager tools, réponse « Failed to find package 'tools' », exit 1.
- Correction isolée : suppression de setup-android@v3, utilisation du sdkmanager préinstallé par ubuntu-latest pour installer platforms;android-36 et build-tools;36.0.0.
- Statut : correctif enregistré, recompilation à vérifier ; aucun défaut Java établi par ce run.

## 2026-10-19 — BUILD-003 : sdkmanager hors du PATH
- Run #39243945614 job 118031180480 : étape « Installer Android SDK 36 » fail 127 : sdkmanager: command not found. La suppression de setup-android a évité BUILD-002 mais n'a pas exporté sdkmanager dans PATH.
- Correction : invoquer l'exécutable du SDK préinstallé via $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager, avec recherche locale dans cmdline-tools si nécessaire. Rebuild à vérifier.

## 2026-10-19 — UI-001 : audit statique de contraste avant essai Samsung
- La première build v0.1.0 #39244099636 a réussi avec APK brut direct (29 071 octets), mais revue statique : dialogue activité récente créait des labels en noir alors que le thème natif est sombre ; risque de texte non lisible. Pas une panne observée matériellement.
- Correction : labels du dialogue en blanc/gris clair, journalisation explicite de la copie locale ; déclenchement CI limité aux modifications source/config pour permettre synchronisation documentaire sans reconstruire à chaque édition des mémoires.
- Une recompilation remplace le binaire temporaire (signature debug de runner éphémère). La nouvelle empreinte devra être relevée après la build ; première empreinte historique = 295a46489f90bd0779ac592989ab25c38f2533b6e3c0497f90b21f5edc7f9a1a.

## 2026-10-19 — STATE-001 : risque de comparer une nouvelle référence à une ancienne mesure « après »
- Revue du code : prefs persistait after et afterTime après lancement d'une nouvelle préparation. UI risquait d'afficher un « après » issu d'une préparation précédente : comparaison trompeuse.
- Correction : à chaque nouveau PREPARATION_DEBUT, supprimer after, afterTime, afterLow avant d'enregistrer le nouveau baseline. L'utilisateur doit refaire une mesure après. Recompilation et vérification de l'APK attendues.

## 2026-10-19 — BUILD-004 : compilation finale vérifiée
- Run #39244803588, job 118034026191 : conclusion success ; Java/Gradle/SDK, assembleDebug, contrôle file APK, publication de la copie temp réussis.
- Branche feature/optimem-v0.1.0-native-memory ; commit CI ayant ajouté le binaire : 25dc96ef577e9809a2add6254e9e7b173c3e4882.
- APK brut 29159 octets. SHA-256 enregistré : 1edea17dae892aaae019199cbfe384c53e9f555c428487b0b721619c3ec77312. URL : https://raw.githubusercontent.com/greenpower2669/OptiMem-/feature/optimem-v0.1.0-native-memory/temp/OptiMem-v0.1.0-debug.apk
- Attention : builds debug signées avec clés de développement éphémères ; ne pas garantir mise à jour en place depuis une APK d'un run antérieur. Désinstallation potentiellement nécessaire, entraînant perte des données locales OptiMem. Une signature durable requiert validation et configuration dédiée.
- Tests matériels Samsung, refus permission / TalkBack / efficacité réelle non exécutés. Ne pas les déclarer conformes avant retour de Fab.

## 2026-10-19 — DIAG-RAMPLUS-002
Observation rapportée : RAM Plus 4→8 Go, écran indiquant environ 2→4 Go « occupés ». Pas de deux relevés horodatés strictement comparables, pas d'attribution possible à une application. Hypothèses : mise en cache/conservation différente, état système différent, différence des indicateurs affichés. Ne pas promettre récupération de 2 Go. Documenté par Samsung : la RAM Plus utilise le stockage en mémoire virtuelle.

## 2026-10-19 — DATA-WATCHLIST-002
Risque confirmé par la chaîne de compilation : chaque APK debug peut être signée différemment sur un runner éphémère. Une réinstallation par désinstallation fait perdre préférences et liste. Correctif fonctionnel proposé : sauvegarde JSON manuelle depuis la nouvelle version (SAF), importation additive après installation, sans serveur. Tests Android à faire avant de considérer l'import/export validé sur téléphone.

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

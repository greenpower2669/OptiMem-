# OptiMem — contrat fonctionnel 0.1.0

## Contexte
Samsung SM-A576B sous Android 16 ; préparer FxExtractor / Stable Audio 3 sans root, ADB, privilèges ni suppression d'application tierce.

## Audit réel
L'API Android ActivityManager.MemoryInfo expose totalMem, availMem, threshold, lowMemory (et advertisedMem depuis API 34). Sur Android 16 API 36, freeMem n'est pas disponible (introduit API 37). totalMem = RAM accessible au noyau, non nécessairement la RAM commercialement annoncée ; availMem tient compte du fonctionnement mémoire du noyau et ne correspond pas à un tas de RAM immédiatement inoccupée. totalMem - availMem est une approximation de mémoire non disponible, incluant potentiellement cache/système ; il est interdit de l'appeler consommation cumulée des applications. La portion récupérable par application est non observable via l'API publique ici.

UsageStatsManager.queryEvents / queryUsageStats demandent un consentement explicite dans Accès aux données d'utilisation. Dernier événement en avant-plan = activité récente, jamais processus confirmé ni RAM tierce mesurée. Sans permission afficher "indisponible" et lien au réglage. Les limitations de visibilité des packages peuvent remplacer les noms par identifiants.

Aucune API Android publique ne permet à une application standard de forcer l'arrêt des autres applications arbitrairement ou de paramétrer Samsung RAM Plus. Ouvrir les réglages Android applicatifs via Intent ACTION_APPLICATION_DETAILS_SETTINGS. Samsung décrit Paramètres > Maintenance de l'appareil > Mémoire > RAM Plus et redémarrage éventuel ; ne pas supposer un raccourci direct garanti.

## Fonctions prévues
- Tableau de bord réactualisé uniquement en activité visible, sans service permanent.
- Courbe locale des snapshots d'observation, échantillonnée toutes les 4 secondes ; ne pas extrapoler des valeurs manquantes.
- Avant/après stocké localement et signalisé comme "variation observée", pas comme RAM libérée causalement.
- Guide RAM Plus 4 et 8 Go avec annotations manuelles (ne peut lire valeur du réglage).
- Journal privé copiable par action utilisateur ; aucune connexion Internet, aucun transfert.
- Sélection manuelle des applications récentes et page paramètres ; sélection d'une application IA installée et lancement.
- Nettoyer seulement cacheDir de ce package, sans stockage externe.
- Design accessible natif, boutons de bonne taille et texte explicite, couleurs non uniques ; icône originale puce sourire/balai.

## Décisions de prudence
Pas de QUERY_ALL_PACKAGES, killBackgroundProcesses, forceStopPackage, dumpsys, commandes shell/ADB, permissions étendues réseau. Pas de lecture /proc d'autres processus. Aucun "tout nettoyer".
L'application est un instrument d'observation et d'aide, jamais un gain de mémoire garanti.

## Sources officielles
https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo
https://developer.android.com/reference/android/app/usage/UsageStatsManager
https://developer.android.com/reference/android/content/Intent#ACTION_APPLICATION_DETAILS_SETTINGS
https://www.samsung.com/hk_en/support/mobile-devices/what-is-ram-plus-and-how-to-use-it/

## Observation d'accessibilité v0.1.0
Dans le thème Android sombre, les libellés de l'activité récente utilisent blanc/gris clair, y compris dans les dialogues ; la copie du journal est consignée avant la lecture du fichier local.

## Invariant de session de préparation
Toute nouvelle commande Préparer réinitialise l'ancienne mesure « après » persistante avant de stocker le nouvel « avant », afin de ne jamais afficher un delta entre deux préparations distinctes.

## OPTIMEM-MISSION-002 — contrat v0.2.0
- Bouton « carnet d'applications à examiner » ; sélection multi-applications visibles par PackageManager et sous liste UsageStats si permission ; cases à cocher, notes, état « vérifié ». Visibilité Android limitée, aucun statut d'exécution/RAM individuelle inféré.
- Stockage JSON privé dans SharedPreferences, sur disque et indépendant des sessions ; aucune permission Internet ; retrait explicite confirmé sans toucher application.
- Pour chaque app : fiche + guide Samsung (veille, veille profonde, restrictions, désinstallation manuelle), lancement de ACTION_APPLICATION_DETAILS_SETTINGS ou page réglages générale de repli ; pas de commande de nettoyage forcé.
- Exclure toute modification automatique des services système, d'accessibilité, de communication et de sécurité ; avertissement notamment sur notifications qui cesseraient.
- Export JSON explicite via ACTION_CREATE_DOCUMENT, import via ACTION_OPEN_DOCUMENT avec validation de schéma et confirmation ; fusion additive, préserver notes locales. La signature debug CI peut exiger une réinstallation avec perte des données : faire sauvegarde AVANT.
- RAM Plus est de la mémoire virtuelle sur stockage. Le passage 4→8 Go peut modifier la conservation des apps et caches mais ne permet pas de prouver que 2 Go de RAM ont été « repris » par celles-ci. Seules mesures globales Android sont disponibles.
Sources officielles : https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo ; https://developer.android.com/guide/topics/providers/document-provider ; https://www.samsung.com/hk_en/support/mobile-devices/what-is-ram-plus-and-how-to-use-it/ ; https://www.samsung.com/ca/support/mobile-devices/galaxy-phone-sleeping-apps/.

## Livraison et fiabilité à distinguer
Le build 0.2.0 passe sur GitHub Actions (#39257212200). Le suivi local, les notes et l'import/export existent dans le code compilé mais leur robustesse sur Samsung n'est pas encore démontrée. Signatures debug non stables : sauvegarde externe explicite nécessaire avant désinstallation.

## OPTIMEM-MISSION-003 — contrat vocal v0.3.0
- Le défaut visuel rapporté par Fab est fonctionnel : ni l'accueil ni le carnet ne lisaient à voix haute malgré l'accessibilité de taille de texte. Correction par TextToSpeech Android natif (classe SpeechGuide).
- Français France, débit initial modéré 0,87 ; voix système choisie par Android. Pas de permission réseau, pas de service, pas d'enregistrement microphone ni synthèse distante pilotée par l'app. Une voix française installée est nécessaire ; sinon afficher un diagnostic et pointer vers la synthèse vocale Android.
- Lecture auto activée par défaut et mémorisée dans `optimem_speech`, ne pas déclencher les annonces automatiques lorsque le lecteur d'écran en exploration tactile est actif. Boutons Lire disponibles même si auto désactivée ; bouton Stop ; arrêter TTS à onPause et shutdown à onDestroy.
- Annoncer état RAM à l'ouverture sans faire parler l'actualisation 4 secondes ; dire conseils et résultat de comparaison lors de l'action. En carnet : annoncer résumé, lire jusqu'à 10 fiches puis proposer lecture individuelle sans discours interminable, lire les notes personnelles et guider les restrictions.
- Aucune affirmation de privilèges Samsung et aucune exécution Android automatique. Autres applications/Paramètres Samsung restent hors du contrôle de la voix d'OptiMem : utiliser TalkBack pour lire les écrans système.
- Si le moteur TTS Android ne possède pas les voix françaises, la lecture locale peut être indisponible ; pas de promesse d'audio tant qu'installée et testée sur SM-A576B.

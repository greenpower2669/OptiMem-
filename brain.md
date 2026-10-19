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

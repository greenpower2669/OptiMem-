# OptiMem 🧹 — 0.1.0 (Android 16)

Application Android native hors ligne pour préparer manuellement le téléphone avant une IA locale exigeante.

## Fonctionnement
- Mémoire : ActivityManager.MemoryInfo (totalMem, availMem, lowMemory, threshold). "Occupée / non disponible" = totalMem - availMem : estimation, pas une somme de RAM par application.
- Observation : relevé toutes les 4 secondes pendant que la fenêtre est ouverte ; historique graphique uniquement dans la session (aucun service).
- Préparation IA : instantané avant, conseils manuels, instantané après ; différence mesurée, sans attribution causale.
- Applications récentes : seulement si l'utilisateur autorise l'accès aux données d'utilisation. Une activité en avant-plan ne prouve PAS un processus encore vivant. RAM de tiers = indisponible.
- Accès aux paramètres pour fermeture/restriction exclusivement manuelle.
- RAM Plus : aide vers Paramètres > Maintenance de l'appareil > Mémoire > RAM Plus (selon One UI). Ni lecture du réglage courant ni changement automatique.
- Cache : nettoyage du dossier cache d'OptiMem uniquement.
- Journal : fichier local privé, copie manuelle au presse-papiers. Pas de permission INTERNET, pas de télémétrie.

## Construction
Compilation GitHub Actions automatique sur la branche feature/optimem-v0.1.0-native-memory.
APK debug, signé par la clé de développement du runner, non destiné à Play.
Livraison directe prévue : temp/OptiMem-v0.1.0-debug.apk et son .sha256 ; aucune Release sans validation de Fab.
AAB non produit à ce stade : pas d'identité Play/clé d'importation persistante configurée.

## Validation
La compilation CI ne constitue pas un test sur le Samsung SM-A576B. Tests matériels et permission usage access à réaliser par Fab.

## Références
- https://developer.android.com/reference/android/app/ActivityManager.MemoryInfo
- https://developer.android.com/reference/android/app/usage/UsageStatsManager
- https://www.samsung.com/hk_en/support/mobile-devices/what-is-ram-plus-and-how-to-use-it/
- https://www.samsung.com/us/support/answer/ANS10001953/

Méthode : https://github.com/greenpower2669/FAB-Copilot-forWorkspacegpt

## Preuve de livraison du 19/10/2026
Compilation finale : https://github.com/greenpower2669/OptiMem-/actions/runs/39244803588 (success).
APK direct : https://raw.githubusercontent.com/greenpower2669/OptiMem-/feature/optimem-v0.1.0-native-memory/temp/OptiMem-v0.1.0-debug.apk
SHA-256 : 1edea17dae892aaae019199cbfe384c53e9f555c428487b0b721619c3ec77312
Attention : la clé debug est éphémère sur GitHub Actions ; un ancien APK debug de ce projet peut nécessiter une désinstallation avant installation de la nouvelle build, ce qui efface les préférences et journaux OptiMem. Aucune Release ni merge main.

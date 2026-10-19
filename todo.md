# OptiMem — suivi réel

## Fait
- [x] Audit de principe Android 16 et Samsung, sources documentées.
- [x] Lecture méthode FAB Copilot et carte Djipie pertinente.
- [x] Dépôt existant confirmé greenpower2669/OptiMem- ; main non modifiée.
- [x] Branche dédiée feature/optimem-v0.1.0-native-memory créée.

## En cours
- [x] Code Java natif et cinq mémoires dans les mêmes commits sur la branche dédiée.
- [x] CI #39244099636 : compile succeeded, APK signé debug direct poussé dans temp/ (SHA provisoire de la première build consignée dans historique).
- [x] CI #39244560569 : build correctif de contraste réussi ; APK brut disponible, nouvelle empreinte disponible par .sha256.
- [x] CI #39244803588 après correctif STATE-001 : succès toutes étapes ; APK temp direct 29159 octets ; SHA-256 1edea17dae892aaae019199cbfe384c53e9f555c428487b0b721619c3ec77312.
- [x] APK brut et fichier .sha256 vérifiés dans temp/ sur la branche de travail. URL directe confirmée par API GitHub.
- [x] Incidents CI initiaux (#39243768981, #39243945614) diagnostiqués et corrigés ; historisés, sans modification main.

## Validation externe demandée à Fab
- [ ] Installer sur Samsung SM-A576B Android 16 et vérifier ouverture/interface.
- [ ] Vérifier valeurs RAM et changement réel observé avant/après sans causalité inventée.
- [ ] Refuser puis autoriser usage access ; vérifier liste et pages paramètres.
- [ ] Naviguer Maintenance Samsung et revenir mesurer, comparer RAM Plus 4 vs 8 Go manuellement.
- [ ] Choisir et lancer FxExtractor, puis tester absence de dégradation.
- [ ] Journal/clipboard, redémarrage, contraste et TalkBack.
- [ ] Tester cache propre uniquement. Ne jamais effacer caches tiers.
- [ ] Publication AAB plus tard si Fab souhaite mise en ligne et configure signature durable.

## Blocages connus
- Dépôt demandé sans tiret absent ; dépôt OptiMem- utilisé car existant, pas de renommage.
- Le matériel de test est chez Fab : absence de preuve physique tant que test non réalisé.
- AAB non approprié à cette étape sans clé Play durable, ne pas prétendre signer une version Play.

## OPTIMEM-MISSION-002 — avancement v0.2.0
- [x] Conserver la nouvelle mission dans ordres-de-mission.md ; relire les quatre mémoires du projet.
- [x] Concevoir carnet persistant, cases à cocher, notes, état vérifié, accès réglages et guide Samsung ; export/import manuel pour éviter pertes de listes après désinstallation.
- [x] Compiler sur GitHub Actions : run #39257212200, job 118075396501, toutes les étapes success.
- [ ] Tester sur SM-A576B la sélection et recherche des applications, visibilité partielle, reprise/redémarrage, notes, retrait confirmé, permissions refusées, backup/import.
- [ ] Tester guidage/Intent vers fiches des apps et réglages de batterie. Ne jamais désactiver services critiques.
- [ ] Comparaison honnête RAM Plus 4 vs 8 à contexte similaire ; ne pas attribuer la différence à un processus.
- [ ] Sécuriser une signature durable pour mises à jour futures, sans publier de clé privée.
- [ ] Aucun merge main/Release/AAB Play sans décision de Fab.

## OPTIMEM-MISSION-003 — TTS
- [x] Identifier la panne fonctionnelle : aucune intégration de synthèse vocale Android antérieure.
- [x] Créer la couche native SpeechGuide, FR, ralentissement, stop et annonce auto avec protection TalkBack ; brancher MainActivity et WatchlistActivity.
- [x] Préserver code, données et méthodes antérieurs, nouvelle branche et noms d'APK.
- [x] Compilation GitHub Actions v0.3.0 réussie (#39301906625, job 118228983503, 50 019 octets). APK brut dans temp/ et checksum SHA-256 `4f1ccc903e13b051f2d69d5d46eddb18d38140cda4d113d4fa8b5ae200409794`, disponibilité vérifiée par GitHub API.
- [ ] Essayer sur Samsung : voix française installée, lecture d'accueil, lire un bilan, arrêter, désactiver auto, lecture d'une fiche puis noter/valider, retour réglages ; vérifier TalkBack pas de doubles lectures et aucune annonce toutes les 4 secondes.
- [ ] Avant désinstallation nécessaire pour nouvelle signature CI, exporter carnet v0.2 depuis son écran. Un import restaure les fiches, pas forcément les mesures ni journaux.
- [ ] Aucune fusion main ni Release sans Fab.

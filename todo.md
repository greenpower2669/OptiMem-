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

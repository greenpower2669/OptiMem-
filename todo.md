# OptiMem — suivi réel

## Fait
- [x] Audit de principe Android 16 et Samsung, sources documentées.
- [x] Lecture méthode FAB Copilot et carte Djipie pertinente.
- [x] Dépôt existant confirmé greenpower2669/OptiMem- ; main non modifiée.
- [x] Branche dédiée feature/optimem-v0.1.0-native-memory créée.

## En cours
- [ ] Code Java natif et cinq mémoires dans un commit cohérent de la branche.
- [ ] Build GitHub Actions : vérifier job concluant avec statut success, ne pas inférer.
- [ ] Vérifier temp/OptiMem-v0.1.0-debug.apk brut et SHA-256 via dépôt, donner URL directe.
- [ ] Si CI échoue, rapporter erreurs dans debughistorical.md et corriger sans toucher main.

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

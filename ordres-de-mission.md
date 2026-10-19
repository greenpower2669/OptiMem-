# OptiMem — ordres de mission

## IMMUTABLE / À PRÉSERVER — FAB-IMMUTABLE-001
- Nom du produit : OptiMem.
- Package proposé au début du projet : fr.fab.optimem (non encore enregistré sur un store ; modification seulement avec aval de Fab si nécessaire).
- Dépôt de travail existant : greenpower2669/OptiMem- (tiret final).
- Branche de développement : feature/optimem-v0.1.0-native-memory ; jamais de fusion main sans accord.
- APK de test : temp/OptiMem-v0.1.0-debug.apk, version 0.1.0 ; AAB non prévu avant configuration de signature de publication.
- Interdit : pas d'accès Internet, télémétrie, effacement d'applications tierces, faux nettoyage ou arrêt forcé des services système.
- Les clés de debug CI sont éphémères ; une future clé stable ne doit jamais être publiée dans Git.

## OPTIMEM-MISSION-001 — Demande de Fab du 19 octobre 2026 (ouverte, à livrer et tester)
Créer une vraie application Android native en français, légère, accessible et hors ligne sur Samsung SM-A576B Android 16.
1. Auditer API Android/One UI sans root ni ADB avant développement, conserver le résultat dans les mémoires.
2. Tableau de bord temps réel tant qu'OptiMem est visible : RAM accessible/disponible, pression, seuil, lowMemory, courbe, avant/après, distinction honnête utilisée / récupérable si exposée.
3. Activité récente avec autorisation système si accordée, avec statuts séparés usage récent/processus confirmé/RAM mesurée/non disponible. Accès manuel aux paramètres des applications choisies. Ne jamais tuer de processus.
4. Gros bouton « Préparer le téléphone pour une IA lourde », mesurer, conseiller, guider vers maintenance Samsung, remesurer et proposer le lancement de FxExtractor ou application choisie, manuellement et réversiblement.
5. Expliquer RAM physique, RAM Plus et mémoire virtuelle ; accompagner comparaison des essais à 4 et 8 Go sans modifier le système.
6. Nettoyer uniquement le cache d'OptiMem, aucune donnée personnelle, sauvegarde, fichier, modèle IA ou cache tiers effacé.
7. Journal local persistant, horodatages, actions, mesures, refus, export Copier le journal ; aucun réseau.
8. Gros boutons, agrandissement, TalkBack, contrastes, libellés, puce souriante avec balai.
9. Respecter FAB-MISSION-001, FAB-MEM-001, FAB-DEBUG-001 et FAB-APK-TEMP-001 : cinq mémoires projet maintenues avec le code.
10. Compiler GitHub Actions : APK debug v0.1.0 direct décompressé côté GitHub sous temp, SHA-256, commit et preuve CI ; AAB si adapté, explicite sur signature. Pas de merge main ni Release.
11. Vérifier sur appareil la lecture, permissions refusées, avant/après, redémarrage, accessibilité et non-dégradation ; Fab conserve la validation matérielle.

Statut : CODE + APK DEBUG LIVRÉS, À VALIDER SUR SAMSUNG. CI final GitHub Actions #39244803588 : success ; APK brut disponible sous temp/ sur branche dédiée, taille 29159 octets ; SHA-256 1edea17dae892aaae019199cbfe384c53e9f555c428487b0b721619c3ec77312. Pas d'AAB signé Play, aucun test matériel revendiqué. Pas de merge main ni Release.

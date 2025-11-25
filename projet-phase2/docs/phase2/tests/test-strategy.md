# Stratégie de tests – Phase 2

| Niveau | Objectif | Couverture / artefacts |
|--------|----------|------------------------|
| **Unit** | Valider les règles métier critiques sans dépendances externes. | Tests Mockito sur `Authenticate`, `DepositFunds`, `PlaceOrder` (`src/test/java/com/brokerx/unit`). |
| **Intégration** | Vérifier la persistance, transactions et idempotence contre PostgreSQL + Redis (Testcontainers). | `AuthIntegrationTest`, `DepositIntegrationTest`, `OrderIntegrationTest`. |
| **E2E** | Démontrer le scénario login → dépôt → ordre via HTTP réel. | `ScenarioE2ETest` (profil `test`, port aléatoire). |

- Couverture visée : ≥80 % sur les services `Authenticate`, `DepositFunds`, `PlaceOrder` (mesurée via JaCoCo lors du rapport final).
- Génération automatique : `mvn verify` (JDK 21 recommandé pour la compatibilité JaCoCo). Rapport HTML : `projet-phase2/target/site/jacoco/index.html`.
- Les tests d’intégration utilisent `jdbc:tc:postgresql:16:///testdb` + un container Redis éphémère pour rejouer les migrations Flyway, vérifier les contraintes (FK, CHECK, unique) et s’assurer que l’éviction du cache fonctionne (`OrderCacheService`).
- Le test E2E orchestre l’appel des endpoints REST et sert de démonstration automatique du scénario de phase 1.
- Pour les futures phases, ajouter des tests de sécurité (JWT expiré, MFA failure) et des tests de performance ciblés sur les contrôles pré-trade.

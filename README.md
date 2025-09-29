# BrokerX – Phase 1

Prototype monolithique (architecture hexagonale) pour la plateforme de courtage BrokerX. Ce dépôt regroupe :

- Documentation Arc42 + 4+1 (`projet-phase1/docs/phase1/arc42/docs.md`).
- Décisions architecturales (`projet-phase1/docs-livrable/docs-livrable1/adr/`).
- Détails de la stratégie de tests (`projet-phase1/docs/phase1/tests/test-strategy.md`).
- Scripts de déploiement & runbook (`projet-phase1/docs/phase1/deploy/README.md`).
- Source code de l’application (`projet-phase1/`).

## Démarrage

### Prérequis
- Docker & Docker Compose
- JDK 21 + Maven (pour développement local)

### Option A – via Docker Compose
```bash
cd projet-phase1
./scripts/deploy.sh         # construit l'image et lance app + PostgreSQL
```
Puis vérifier :
```bash
curl http://localhost:8080/health
```
Scénario de démo :
```bash
curl -X POST http://localhost:8080/internal/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"seed@brokerx.dev","password":"password123","otp":"123456"}'

curl -X POST http://localhost:8080/internal/deposits -H 'Content-Type: application/json' \
  -d '{"accountId":"11111111-1111-1111-1111-111111111111","amount":100,"idempotencyKey":"demo"}'

curl -X POST http://localhost:8080/internal/orders -H 'Content-Type: application/json' \
  -d '{"accountId":"11111111-1111-1111-1111-111111111111","side":"BUY","type":"MARKET","symbol":"AAPL","qty":10,"clientOrderId":"demo-order"}'
```
Arrêt : `docker compose down`.

### Interface web de démonstration
Une fois l'application démarrée (Docker ou `mvn spring-boot:run`), ouvrir `http://localhost:8080/index.html`.

La page fournit trois formulaires :
- **Connexion** (`/internal/auth/login`)
- **Dépôt virtuel** (`/internal/deposits`)
- **Placement d'ordre** (`/internal/orders`)

Le panneau "Journal" affiche les réponses des endpoints (succès/erreurs) pour la démonstration.

### Option B – dev local
```bash
cd projet-phase1
mvn clean test                 # exécute unit/int/E2E (Testcontainers)
mvn verify                     # génère le rapport JaCoCo (utiliser JDK 21)
mvn spring-boot:run            # nécessite PostgreSQL local (voir application.yml)
```

## CI/CD
- Pipeline GitHub Actions : `.github/workflows/ci.yml` (build + tests + artefact).
- Conteneurisation multi-stage (`projet-phase1/Dockerfile`), stack compose (`docker-compose.yml`).
- Script de déploiement : `projet-phase1/scripts/deploy.sh`.
- Documentation consolidée : `projet-phase1/docs/phase1/Documentation.md` (Arc42 + stratégie de tests + runbook).

### Rapports & Livrables supplémentaires
- Couverture JaCoCo : `projet-phase1/target/site/jacoco/index.html` (généré lors de `mvn verify` avec JDK 21).
- ADR consolidés : `projet-phase1/docs-livrable/docs-livrable1/adr/`.

## Références documentation
- Analyse métier & vues : `projet-phase1/docs/phase1/arc42/docs.md`
- Pyramide de tests : `projet-phase1/docs/phase1/tests/test-strategy.md`
- Runbook : `projet-phase1/docs/phase1/deploy/README.md`
- Modèle de domaine PlantUML : `projet-phase1/docs/phase1/analysis/domain-model.puml`

# BrokerX – Remise finale (architecture event-driven)

[![CI](https://github.com/AymanZahir/brokerX-phase1/actions/workflows/ci.yml/badge.svg)](https://github.com/AymanZahir/brokerX-phase1/actions/workflows/ci.yml)

Prototype monolithique (architecture hexagonale) pour la plateforme de courtage BrokerX. Ce dépôt regroupe :

- Documentation Arc42 + 4+1 (`brokerx/docs/architecture/arc42/docs.md`).
- Décisions architecturales (`brokerx/docs-livrable/docs-livrable1/adr/`).
- Détails de la stratégie de tests (`brokerx/docs/architecture/tests/test-strategy.md`).
- Scripts de déploiement & runbook (`brokerx/docs/architecture/deploy/README.md`).
- Contrat OpenAPI exporté (`brokerx/docs/api/brokerx-openapi.json`).
- Collection Postman (`brokerx/docs/api/BrokerX.postman_collection.json`).
- Documentation de transformation event-driven (remise finale) : `brokerx/docs/event-driven/architecture-event-driven.md`, `brokerx/docs/event-driven/observability.md`, `brokerx/docs/event-driven/ui-mfa.md`.
- Source code de l’application (`brokerx/`).
- Flux temps réel (phase 3) :
  - SSE marché (UC-04) : `GET /api/v1/market/stream?symbol=AAPL` (publique via Kong 8081).
  - SSE notifications (UC-08) : `GET /api/v1/notifications/stream` (JWT requis).
  - Grafana sur `http://localhost:3001` (dashboard Golden Signals provisionné).

## Démarrage

### Prérequis
- Docker & Docker Compose
- JDK 21 + Maven (pour développement local)

### Option A – via Docker Compose
```bash
cd brokerx
./scripts/deploy.sh         # construit l'image et lance app + PostgreSQL + Redis + Prometheus + Grafana + Gateway + microservices dédiés
```
Puis vérifier :
```bash
curl http://localhost:8081/monolith/actuator/health
```
Scénario de démo :
```bash
curl -X POST http://localhost:8081/api/v1/auth/signup -H 'Content-Type: application/json' \
  -d '{"email":"new-user@brokerx.dev","password":"Password!23","fullName":"New Trader","phone":"+15145550123","address":"123 Demo Street, QC","country":"CA","dateOfBirth":"1995-04-10"}'

# récupérer l'OTP renvoyé (profil test) puis confirmer :
curl -X POST http://localhost:8081/api/v1/auth/signup/confirm -H 'Content-Type: application/json' \
  -d '{"accountId":"<ACCOUNT_ID>", "otp":"<OTP>"}'

TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"email":"seed@brokerx.dev","password":"password123","otp":"123456"}' | \
  python3 -c 'import sys,json; print(json.load(sys.stdin)["token"])')
echo "Bearer token: $TOKEN"

curl -X POST http://localhost:8081/api/v1/deposits -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"accountId":"11111111-1111-1111-1111-111111111111","amount":100,"idempotencyKey":"demo"}'

curl -X POST http://localhost:8081/api/v1/orders -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"accountId":"11111111-1111-1111-1111-111111111111","side":"BUY","type":"MARKET","symbol":"AAPL","qty":10,"clientOrderId":"demo-order"}'

# remplacer (ex: augmenter la quantité)
curl -X PUT http://localhost:8081/api/v1/orders/<ORDER_ID> -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"expectedVersion":0,"qty":15}'

# annuler
curl -X DELETE http://localhost:8081/api/v1/orders/<ORDER_ID> -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"expectedVersion":1}'

# consulter les exécutions & notifications
curl http://localhost:8081/api/v1/orders/<ORDER_ID>/executions -H "Authorization: Bearer $TOKEN"
curl http://localhost:8081/api/v1/orders/<ORDER_ID>/notifications -H "Authorization: Bearer $TOKEN"
```
> Astuce : l’API Gateway (`http://localhost:8081`) route désormais vers les microservices dédiés : `/api/v1/auth/**` → `auth-service` (profil `auth`, port local 8086), `/api/v1/deposits` → `portfolio-service` (profil `portfolio`, port 8087), `/api/v1/orders/**` → `orders-service` (profil `orders`, port 8088). Le monolithe reste accessible via `http://localhost:8081/monolith/...` ou directement sur `http://localhost:8085`. Redis est fourni pour le cache (`redis://localhost:6379`), Prometheus sur `http://localhost:9090` et Grafana sur `http://localhost:3001` (identifiants par défaut `admin` / `admin`). Pense à changer le mot de passe après import du dashboard BrokerX.
Arrêt : `docker compose down`.

### Interface web de démonstration
- Via Docker Compose : `http://localhost:8085/index.html` (instance `api1` mappée sur le port hôte 8085).
- En dev local (`mvn spring-boot:run`) : `http://localhost:8080/index.html` (port par défaut Spring).

La page fournit trois formulaires :
- **Connexion** (`/api/v1/auth/login`)
- **Dépôt virtuel** (`/api/v1/deposits`)
- **Placement d'ordre** (`/api/v1/orders`)

Le panneau "Journal" affiche les réponses des endpoints (succès/erreurs) pour la démonstration.

### Mode Microservices

Le `docker-compose.yml` lance trois services spécialisés en plus du monolithe :

| Service | Profil Spring | Port hôte | Routes Gateway |
|---------|----------------|-----------|----------------|
| `auth-service` | `auth` | `8086` | `/api/v1/auth/**` |
| `portfolio-service` | `portfolio` | `8087` | `/api/v1/deposits` |
| `orders-service` | `orders` | `8088` | `/api/v1/orders/**` |

Le monolithe reste disponible (`api1`/`api2`, profil `monolith`) pour comparaisons A/B via `http://localhost:8081/monolith/...`. Kong (port 8081) distribue et sécurise les appels entrants.

### Option B – dev local
```bash
cd brokerx
mvn clean test                 # exécute unit/int/E2E (Testcontainers)
mvn verify                     # génère le rapport JaCoCo (utiliser JDK 21)
mvn spring-boot:run            # nécessite PostgreSQL local (voir application.yml)
```

> ⚠️ Les tests d’intégration utilisent Testcontainers (PostgreSQL). Docker doit être disponible
> localement (`/var/run/docker.sock`). Sans Docker, seul `mvn -DskipTests package` fonctionnera
> (cf. limitations de l’environnement d’évaluation).

### Observabilité & Golden Signals
- Endpoint Prometheus : `curl http://localhost:8080/actuator/prometheus`
- OpenAPI : `http://localhost:8080/v3/api-docs` (Swagger UI `http://localhost:8080/swagger-ui/index.html`)
- Métriques clés : `brokerx_orders_accepted_total`, `brokerx_matching_duration_seconds`, `brokerx_matching_executions_total`, `brokerx_matching_qty`, `brokerx_notifications_total`
- Dashboard Grafana : pré-chargé via la provisioning Grafana (`BrokerX Golden Signals`) et disponible sur http://localhost:3001
- Prometheus UI : http://localhost:9090 (scrape `api1`/`api2` toutes les 5 s)
- Charge synthétique : `k6 run tests/perf/orders-matching.js --vus 10 --duration 30s`
- Comparatif perf (k6, 5 VU / 30s) :
  - Gateway (8081) : p90 ≈ 99 ms, p95 ≈ 183 ms, ~8.6 req/s, 0 % erreurs.
  - Direct api1 (8085) : p90 ≈ 83 ms, p95 ≈ 182 ms, ~8.7 req/s, 0 % erreurs.

## CI/CD
- Pipeline GitHub Actions : `.github/workflows/ci.yml` (build + tests + artefact).
- Badge CI : `https://github.com/AymanZahir/brokerX-phase1/actions/workflows/ci.yml` (déjà configuré ci-dessus). La pipeline exécute `mvn -B verify` et charge le rapport JaCoCo en artefact.
- Conteneurisation multi-stage (`brokerx/Dockerfile`), stack compose (`docker-compose.yml`).
- Script de déploiement : `brokerx/scripts/deploy.sh`.
- Documentation consolidée : `brokerx/docs/architecture/Documentation.md` (Arc42 + stratégie de tests + runbook).

### Rapports & Livrables supplémentaires
- Couverture JaCoCo : `brokerx/target/site/jacoco/index.html` (généré lors de `mvn verify` avec JDK 21).
- ADR consolidés : `brokerx/docs-livrable/docs-livrable1/adr/`.

## Références documentation
- Analyse métier & vues : `brokerx/docs/architecture/arc42/docs.md`
- Pyramide de tests : `brokerx/docs/architecture/tests/test-strategy.md`
- Runbook : `brokerx/docs/architecture/deploy/README.md`
- Modèle de domaine PlantUML : `brokerx/docs/architecture/analysis/domain-model.puml`

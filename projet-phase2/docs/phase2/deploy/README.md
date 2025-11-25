# Déploiement & exploitation – Phase 2

## Pré-requis
- Docker & Docker Compose
- JDK 21 (pour exécution locale sans conteneur)

## Lancer la stack
```bash
cd projet-phase2
./scripts/deploy.sh
```
Ce script :
1. Construit l'image multi-stage définie dans `Dockerfile`.
2. Lance `docker compose` avec PostgreSQL, Redis, Prometheus, Grafana, l’API Gateway (Kong) et quatre instances API (`api1`, `api2`, `auth-service`, `portfolio-service`, `orders-service`).
3. Recrée les services en cas de mise à jour (rollback simple : `docker compose down && ./scripts/deploy.sh`).

### Déploiement express (évaluateur)
1. `git clone https://github.com/AymanZahir/brokerX-phase1.git && cd brokerX-phase1/projet-phase2`
2. `./scripts/deploy.sh && docker compose up -d`
3. Health-checks clés : `curl http://localhost:8081/monolith/actuator/health`, `curl http://localhost:8081/api/v1/auth/login` (commande README pour récupérer un JWT), `curl http://localhost:8086/actuator/health`.
4. Importer la collection Postman `docs/api/BrokerX.postman_collection.json` (variable `base_url = http://localhost:8081`).
5. Observabilité : Grafana `http://localhost:3000` (admin/admin) et Prometheus `http://localhost:9090`.
6. Arrêt : `docker compose down`.

## Vérifier la santé
- Base de données : healthcheck `pg_isready` intégré au service `db`.
- API (monolithe) : `http://localhost:8085/actuator/health` ou via Gateway `http://localhost:8081/monolith/actuator/health`.
- Microservices :
  - Auth `http://localhost:8086/actuator/health`
  - Portefeuille `http://localhost:8087/actuator/health`
  - Ordres `http://localhost:8088/actuator/health`
- Logs : `docker compose logs -f <service>` (ex. `auth-service`, `orders-service`, `gateway`).
- Contrats API : `docs/api/brokerx-openapi.json` (export du `/v3/api-docs`, importable dans Postman/Swagger UI).
- Collection Postman : `docs/api/BrokerX.postman_collection.json` (variables `base_url`, `jwt`, `orderId`).

## Scénario de smoke test
1. `curl -X POST http://localhost:8081/api/v1/auth/signup ...` puis `POST /api/v1/auth/signup/confirm` avec l'OTP.
2. `curl -X POST http://localhost:8081/api/v1/auth/login ...` (récupérer le JWT retourné).
3. `curl -X POST http://localhost:8081/api/v1/deposits ...` (avec `Authorization: Bearer <token>`).
4. `curl -X POST http://localhost:8081/api/v1/orders ...` (avec JWT).
5. Facultatif : `curl -X PUT http://localhost:8081/api/v1/orders/{orderId}` avec `{"expectedVersion":0,"qty":15}`
6. Facultatif : `curl -X DELETE http://localhost:8081/api/v1/orders/{orderId}` avec `{"expectedVersion":1}`
7. Visualiser les exécutions `curl http://localhost:8081/api/v1/orders/{orderId}/executions`
8. Visualiser les notifications `curl http://localhost:8081/api/v1/orders/{orderId}/notifications`

> N'oubliez pas d’ajouter le header `Authorization: Bearer <token>` pour les endpoints `deposits` et `orders`.
Les commandes sont documentées dans la section démonstration.

## Environment variables
| Variable | Description | Par défaut |
|----------|-------------|------------|
| `DB_URL` | URL JDBC PostgreSQL | `jdbc:postgresql://db:5432/brokerx` (dans compose) |
| `DB_USERNAME` | Utilisateur BD | `brokerx` |
| `DB_PASSWORD` | Mot de passe BD | `brokerx` |

Pour override en production : `docker compose --env-file prod.env up -d`.

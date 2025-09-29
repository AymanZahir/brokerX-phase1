# Déploiement & exploitation – Phase 1

## Pré-requis
- Docker & Docker Compose
- JDK 21 (pour exécution locale sans conteneur)

## Lancer la stack
```bash
cd projet-phase1
./scripts/deploy.sh
```
Ce script :
1. Construit l'image multi-stage définie dans `Dockerfile`.
2. Lance `docker compose` avec l'application (`app`) et PostgreSQL (`db`).
3. Recrée les services en cas de mise à jour (rollback simple : `docker compose down && ./scripts/deploy.sh`).

## Vérifier la santé
- Base de données : healthcheck `pg_isready` intégré au service `db`.
- Application : healthcheck `http://localhost:8080/health` (exposé via `HealthController`).
- Logs : `docker compose logs -f app` ou `docker compose logs -f db`.

## Scénario de smoke test
1. `curl -X POST http://localhost:8080/internal/auth/login ...`
2. `curl -X POST http://localhost:8080/internal/deposits ...`
3. `curl -X POST http://localhost:8080/internal/orders ...`
Les commandes sont documentées dans la section démonstration.

## Environment variables
| Variable | Description | Par défaut |
|----------|-------------|------------|
| `DB_URL` | URL JDBC PostgreSQL | `jdbc:postgresql://db:5432/brokerx` (dans compose) |
| `DB_USERNAME` | Utilisateur BD | `brokerx` |
| `DB_PASSWORD` | Mot de passe BD | `brokerx` |

Pour override en production : `docker compose --env-file prod.env up -d`.

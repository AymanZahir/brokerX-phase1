# Mises à jour OpenAPI / Postman – Phase 3

- Ajout UC-04 flux marché (SSE) : `GET /api/v1/market/stream?symbol=AAPL` (publique via Gateway 8081).
- Ajout UC-08 notifications SSE : `GET /api/v1/notifications/stream` (JWT requis). Exemple : se connecter (`/api/v1/auth/login`) puis `curl -N -H "Authorization: Bearer <token>" 'http://localhost:8081/api/v1/notifications/stream'`.
- Grafana exposé sur `http://localhost:3001` (dashboard Golden Signals provisionné).
- Résumé perf k6 (5 VU / 30s) :
  - Gateway (8081) : p90 ≈ 99 ms, p95 ≈ 183 ms, ~8.6 req/s, 0 % erreurs.
  - Direct api1 (8085) : p90 ≈ 83 ms, p95 ≈ 182 ms, ~8.7 req/s, 0 % erreurs.

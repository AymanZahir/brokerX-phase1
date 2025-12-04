# Observabilité et traçage (OpenTelemetry + Jaeger)

Objectif : tracer les flux clés (signup, dépôt, ordre → exécution → notification) sur plusieurs services et relier métriques ↔ traces pour la démonstration finale.

## 1. Activation OTel (Spring Boot)
- Ajouter le starter ou Java agent OTel à chaque service : jar agent dans `./monitoring/otel/agent.jar`.
- Variables communes (docker compose ou env) :
  - `OTEL_SERVICE_NAME=<nom-service>`
  - `OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger:4317`
  - `OTEL_METRICS_EXPORTER=prometheus`
  - `OTEL_PROPAGATORS=tracecontext,baggage`
  - `OTEL_RESOURCE_ATTRIBUTES=deployment.environment=demo,team=brokerx`
- Lancer avec agent : `JAVA_TOOL_OPTIONS="-javaagent:/opt/otel/agent.jar"` ou `OTEL_JAVAAGENT_ENABLED=true`.

## 2. Jaeger (collector + UI)
- Ajouter au compose :
```yaml
  jaeger:
    image: jaegertracing/all-in-one:1.58
    ports:
      - "16686:16686"   # UI
      - "4317:4317"     # OTLP gRPC
    environment:
      - COLLECTOR_OTLP_ENABLED=true
```
- Vérifier : http://localhost:16686 → rechercher `orders-service`, `notification-service`, etc.

## 3. Propagation sur le bus d’événements
- Inclure les headers `traceparent` et `baggage` dans chaque message (Kafka headers ou RabbitMQ properties).
- Lors de la consommation, démarrer une nouvelle span enfant du `traceparent` reçu et injecter les en-têtes vers les appels sortants (HTTP ou events émis).
- Pour les handlers concurrents, utiliser une clé d’idempotence (partitionKey) pour garder l’ordre quand nécessaire (ex : `orderId`).

## 4. Métriques & exemplars
- Exposer `/actuator/prometheus` par service; Prometheus scrappe via compose (voir `monitoring/prometheus.yml`).
- Activer les exemplars pour lier métriques ↔ traces (`management.otlp.tracing.export.enabled=true`).
- Golden Signals à suivre : `http_server_requests_seconds`, `brokerx_orders_accepted_total`, `brokerx_matching_duration_seconds`, `brokerx_notifications_total`.

## 5. Guide de vérification avant la démo
- Faire un `signup` + `verify-email` → vérifier dans Jaeger la chaîne `gateway → auth-service → notification-service`.
- Placer un `deposit` puis un `order` → vérifier spans `gateway → orders-service → portfolio-service → notification-service`.
- Contrôler que les logs structurés incluent `traceId`/`spanId`.
- Conserver 1 capture d’écran Jaeger par flux (auth, dépôt, ordre) pour le rapport.

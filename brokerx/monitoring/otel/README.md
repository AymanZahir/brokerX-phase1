Place here the OpenTelemetry Java agent jar (e.g. `opentelemetry-javaagent.jar` from https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases).  
Already downloaded: `agent.jar` (v1.31.0).  
The Dockerfile copies it to `/opt/otel/agent.jar` and launches the app with `-javaagent:/opt/otel/agent.jar`.

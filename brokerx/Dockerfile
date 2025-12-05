# === Build stage ===
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# === Runtime stage ===
FROM eclipse-temurin:21-jre
WORKDIR /opt/brokerx
RUN apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*
COPY monitoring/otel/agent.jar /opt/otel/agent.jar
COPY --from=build /app/target/brokerx-phase2-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -javaagent:/opt/otel/agent.jar -jar /opt/brokerx/app.jar"]

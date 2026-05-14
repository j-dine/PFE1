FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY api-gateway/ api-gateway/
COPY discovery-service/ discovery-service/
COPY user-service/ user-service/
COPY dossier-service/ dossier-service/
COPY workflow-service/ workflow-service/
COPY paiement-service/ paiement-service/
COPY notification-service/ notification-service/
COPY document-service/ document-service/

RUN mvn install -f pom.xml -pl api-gateway -am -DskipTests \
    -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.httpconnectionManager.ttlSecs=60

FROM eclipse-temurin:17
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /build/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

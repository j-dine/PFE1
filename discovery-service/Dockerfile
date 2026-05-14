FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
COPY discovery-service/ discovery-service/
COPY api-gateway/ api-gateway/
COPY user-service/ user-service/
COPY dossier-service/ dossier-service/
COPY workflow-service/ workflow-service/
COPY paiement-service/ paiement-service/
COPY notification-service/ notification-service/
COPY document-service/ document-service/

RUN mvn install -f pom.xml -pl discovery-service -am -DskipTests \
    -Dhttp.keepAlive=false \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.httpconnectionManager.ttlSecs=60

FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /build/discovery-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

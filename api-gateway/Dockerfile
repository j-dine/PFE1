FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN mvn -q -DskipTests -f api-gateway/pom.xml dependency:go-offline

COPY api-gateway/src api-gateway/src
RUN mvn -q -DskipTests -f api-gateway/pom.xml package

FROM eclipse-temurin:17
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/api-gateway/target/*SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

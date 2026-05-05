FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY api-gateway/src api-gateway/src

RUN mvn -f api-gateway/pom.xml -DskipTests package

FROM eclipse-temurin:17
WORKDIR /app
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /build/api-gateway/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

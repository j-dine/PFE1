FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Cache dependencies
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -q -DskipTests -T1C package

FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /workspace/target/*SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

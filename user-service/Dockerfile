FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml .
COPY user-service/pom.xml user-service/pom.xml
RUN mvn -q -DskipTests -f user-service/pom.xml dependency:go-offline

COPY user-service/src user-service/src
RUN mvn -q -DskipTests -f user-service/pom.xml package

FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /workspace/user-service/target/*SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

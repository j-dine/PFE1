FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml pom.xml
COPY user-service/pom.xml user-service/pom.xml
COPY user-service/src user-service/src

RUN mvn -f user-service/pom.xml -DskipTests package

FROM eclipse-temurin:17
WORKDIR /app
COPY --from=build /build/user-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

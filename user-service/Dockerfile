FROM eclipse-temurin:17
WORKDIR /app

# Use the jar produced by a local Maven build
COPY target/user-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

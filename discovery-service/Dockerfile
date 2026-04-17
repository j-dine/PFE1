# Utiliser une image JDK 17
FROM eclipse-temurin:17
WORKDIR /app

# Copier le jar construit par Maven
COPY target/discovery-service-0.0.1-SNAPSHOT.jar app.jar

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
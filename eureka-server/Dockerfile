# Use Eclipse Temurin (Java 17)
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy JAR file
COPY target/*.jar app.jar

# Expose port
EXPOSE 8761

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
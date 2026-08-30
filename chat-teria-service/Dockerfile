FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/chat-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8085 8099

ENTRYPOINT ["java", "-jar", "app.jar"]
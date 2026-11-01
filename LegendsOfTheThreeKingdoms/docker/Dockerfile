FROM eclipse-temurin:17-jre
LABEL project=three-kingdoms
WORKDIR /app
COPY spring/target/spring-0.0.1-SNAPSHOT.jar ./app.jar
ENTRYPOINT ["sh", "-c", "java  -jar app.jar"]

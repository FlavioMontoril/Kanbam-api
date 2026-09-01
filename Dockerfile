FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY src /app/src
COPY pom.xml /app

RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring && chown spring:spring /app

COPY --chown=spring:spring --from=build /app/target/aprendizado-0.0.1-SNAPSHOT.jar /app/app.jar

USER spring

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

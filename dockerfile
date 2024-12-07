FROM openjdk:17-jdk-slim as build

WORKDIR /srv/package/

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN ./mvnw dependency:go-offline

COPY src src/

RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /srv/package/

COPY --from=build /srv/package/target/weather-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

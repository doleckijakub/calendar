FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /opt/calendar

COPY . .

RUN ./mvnw clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /opt/calendar

COPY --from=build /opt/calendar/target/calendar-0.0.1-SNAPSHOT.jar calendar.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "calendar.jar"]
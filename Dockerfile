# syntax=docker/dockerfile:experimental

FROM maven:3.6.3-jdk-11 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2/repository mvn clean package -DskipTests

FROM openjdk:11-jre
EXPOSE 8090
WORKDIR /app
CMD exec java $JAVA_OPTS -jar pay-api.jar
COPY --from=build /build/target/pay-api*.jar /app/pay-api.jar

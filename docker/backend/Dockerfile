# API Spring Boot. Contexto de build = raiz do repositório Easy-Eats-Api
# (ver docker/docker-compose.yml).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Dependências primeiro: só baixa de novo quando o pom.xml mudar.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B -DskipTests package \
    && cp target/*.jar app.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S easyeats && adduser -S easyeats -G easyeats
COPY --from=build /app/app.jar app.jar
USER easyeats

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

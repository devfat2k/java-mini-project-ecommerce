## Stage 1: Build
#FROM maven:3.9-eclipse-temurin-21 AS builder
#WORKDIR /app
#COPY pom.xml .
#COPY .mvn/ .mvn/
#COPY mvnw .
#RUN ./mvnw dependency:go-offline -B
#COPY src ./src
#RUN ./mvnw clean package -DskipTests
#
## Stage 2: Runtime
#FROM eclipse-temurin:21-jre-alpine
#WORKDIR /app
#COPY --from=builder /app/target/*.jar app.jar
#EXPOSE 8085
#ENTRYPOINT ["java", "-jar", "app.jar"]

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache wget
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
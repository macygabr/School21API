FROM eclipse-temurin:21-jdk-alpine AS build
RUN apk add --no-cache maven

WORKDIR /app

COPY pom.xml .

COPY src ./src

RUN mvn clean package

CMD ["java", "-jar", "target/School21-0.0.1.jar"]


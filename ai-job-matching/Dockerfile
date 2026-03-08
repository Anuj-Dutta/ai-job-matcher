FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

COPY ai-job-matching/pom.xml .

RUN mvn -B -q -e -DskipTests dependency:go-offline

COPY ai-job-matching/src ./src

RUN mvn -B -DskipTests package


FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]

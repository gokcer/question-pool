# 1) Build the Vue frontend
FROM node:22-alpine AS web
WORKDIR /web
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# 2) Build the Spring Boot jar with the frontend inside it
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml ./
RUN mvn -q -B dependency:go-offline
COPY backend/src ./src
COPY --from=web /web/dist ./src/main/resources/static
RUN mvn -q -B -DskipTests package

# 3) Small runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
# Chapter texts: read on start-up for chapters that have no text yet
COPY chapters /chapters
ENV CHAPTER_DOCS_DIR=/chapters
USER 1000
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

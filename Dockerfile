FROM openjdk:12 AS BUILD_STAGE
RUN mkdir -p /app/src/main/java
WORKDIR /app/
COPY build.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew build -x :bootJar -x test --continue
COPY . .
RUN ./gradlew build

FROM ubuntu:16.04
WORKDIR /opt/java-app
COPY --from=BUILD_STAGE /app/build/libs/ .
EXPOSE 8080
CMD ["java","-jar","formula-api.jar"]
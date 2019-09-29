FROM openjdk:11 AS BUILD_STAGE
RUN mkdir -p /app/src/main/java
WORKDIR /app/
COPY build.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew build -x :bootJar -x test --continue
COPY . .
RUN ./gradlew build

FROM ubuntu:18.04
RUN apt-get update && apt-get install --yes --no-install-recommends \
  openjdk-11-jdk \
  iputils-ping \
  iftop \
  telnet \
  curl \
  vim
WORKDIR /opt/java-app
COPY --from=BUILD_STAGE /app/build/libs/ .
EXPOSE 8080
CMD ["java", "-jar", "formula-api.jar"]
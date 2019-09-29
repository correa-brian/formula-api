FROM openjdk:12 AS BUILD_STAGE
RUN mkdir -p /app/src/main/java
WORKDIR /app/
COPY build.gradle gradlew ./
COPY gradle gradle
RUN ./gradlew build -x :bootJar -x test --continue
COPY . .
RUN ./gradlew build

EXPOSE 8080

CMD ["java", "-jar", "build/libs/formula-api.jar"]
# Formula API

### Setup
- Clone the repository
- Copy `docker-compose.yml` to an override docker file `cp docker-compose.yml docker-compose.override.yml` and fill in environment variables
- Start the webserver using command below or as a gradle project in an IDE

```
docker-compose up
```

- Access API at http:localhost:8080

#### Extra Commands / Resources

- Using gradle wrapper

```
./gradlew build
./gradlew run
```


- Via executable jar

```
./gradlew build
cd build/libs
java -jar formula-api.jar
```

- Debugging container

```
 docker run -it -p 8080:8080 formula-api sh
 ```

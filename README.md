# Formula API

### Setup: 
- Clone the repository
- Start the webserver

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
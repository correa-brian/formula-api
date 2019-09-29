# Formula API

### Setup: 
```
./gradlew build
./gradlew run
```


### Executable jar

```
./gradlew build
cd build/libs
java -jar formula-api.jar
```

#### docker-compose
```
docker-compose up
```

- visit localhost:8080

#### Debug container FS

```
 docker run -it -p 8080:8080 formula-api sh
 ```
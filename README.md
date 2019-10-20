# TODO Application

This is an example application based on a Todo list where the different tasks are created, read, updated, or deleted from the database. 
Default this application uses an in-memory database called H2 in dev workflow and PostgresSQL in production. 
It also uses Keycloak (a docker container exposed on `https://localhost:8180` ) for user authentication (use the `keycloak-create-realm` project to create `machi` user with `password`). 

NB: Keycloak container is not started automatically on development environment, use `docker-compose -f docker/keycloak-service.yml up` to start it.

## Populating Keycloak
```bash
cd keycloak-create-realm
mvn clean package
java -jar targe/keycloak-create-realm-1.0-SNAPSHOT-shaded.jar
``` 

## Development mode using in-memory H2 database
DevMode:
```bash
cd machi-todo-api
mvn compile quarkus:dev
```
Then, open: http://localhost:8080/

## Quick compilation and run on a JVM using PostgresSQL database

```bash
cd machi-todo-api
mvn package -DskipTests -DskipITs
java -jar machi-todo-api/target/machi-todo-api-1.0.0-SNAPSHOT-runner.jar
```

Then, open: http://localhost:8080/

## Quick compilation and run on a JVM container
Compile:
```bash
cd machi-todo-api
mvn package -DskipTests -DskipITs
```

Run:
```bash
docker-compose -f docker/docker-compose-jvm.yml up
```

## Compile to Native and run with PostgresSQL ( in a container )

Compile:
```bash
cd machi-todo-api
mvn package -Pnative -Dnative-image.docker-build=true
```
Run:
```bash
docker-compose -f docker/docker-compose-native.yml up
```

## Health check

The api expose a readness check endpoint at `/health/ready` and a liveness check at `/health/live`
 

## Open API

OpenAPI schemas are exposed at `/openapi` 

### swagger 

To view the OpenAPi Swagger UI, go the following endpoint `/swagger-ui`

## Metrics

API metrics are exposed at `/metrics` 
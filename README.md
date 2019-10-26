# TODO API

This is an example application based on a Todo API written using [Quarkus, cloud native Java Stack](https://quarkus.io). 
The API permits users to perform basic operation on the TODO list i.e tasks creation, read, updated, or deleted from the database. 
Default this application uses an in-memory database called H2 in dev workflow and testing PostgresSQL in production. 

## OIDC Authentication with Keycloak
The application uses OpenID Connect for user authentication and authorization to protect the rest resources using bearer token authorization scheme. 
The authorization tokens are issued by OpenId Connect and OAuth 2.0 compliant Authorization Servers such as Keycloak.

Bearer Token Authorization is the process of authorizing HTTP requests based on the existence and validity of a token which 
provides valuable information to determine the subject of the call as well whether or not a HTTP resource can be accessed depending 
on user roles.

The application manages two types roles - `admin` and `users`. User with `admin` role can see all TODO items of all users, 
and they can delete all TODO items. Normal users i.e with a `users` role can create new TODO item, update and delete their items, 
complete an item, list and filter their items.

By default the application is configured to connect to a local (localhost:8180) running [Keycloak server](https://www.keycloak.org/about.html), an open source 
authentication server allowing single sign-on. You can optionally override authentication server url and the client id , and client secret by using providing 
a full path (endpoint) to any other authentication `realm` from an authentication server that is OpenId Connect and OAuth 2.0 compliant.

See the example below:
```bash
export QUARKUS_OIDC_CLIENT_ID=<client-id>
export QUARKUS_OIDC_CREDENTIALS_SECRET=<client-secret>
export QUARKUS_OIDC_AUTH_SERVER_URL=http://<auth-server-url>/auth/realms/<realm-name>
```

See [Quarkus OIDC security Guide](https://quarkus.io/guides/oidc-guide) for more information.

### Populating Keycloak (creating realm and test users)
To launch the Keycloak server locally use the following command:
```bash
docker-compose -f docker/backing-services.yml up keycloak
``` 

The above command will start a Keycloak server with an empty DB. For testing and running our application locally we'll need to register our application (this consists of creating a client), authentication realm for our application 
and populate the realm with few users. To do so, use the following command to create a java executable and luanch it. 
```bash
cd keycloak-create-realm
./mvnw clean package
java -jar targe/keycloak-create-realm-1.0-SNAPSHOT-shaded.jar
``` 

Running the above command will create a `machi-todo-api` client_id with having `secret` as the client_secret. 
It will also create create a `todo-api` realm and populate it with an admin user having the following credentials: 
username `admin` password `secret`, a normal user i.e with `users` role will be created with the following 
credentials: username `machi`, password `secret`. 

#### Retrieving an access token
You'll need to retrieve the access token from Keycloak auth server `https://localhost:8180/auth/realms/todo-api/protocol/openid-connect/token`. 
The following curl commands should suffice

admin:
```bash
curl -d "client_id=machi-todo-api" -d "client_secret=secret" "username=admin" -d "password=machi" -d "grant_type=password" "http://localhost:8080/auth/realms/master/protocol/openid-connect/token" | jq -r '.access_token')
``` 

machi:
```bash
curl -d "client_id=machi-todo-api" -d "client_secret=secret" -d "username=machi" -d "password=machi" -d "grant_type=password" "http://localhost:8080/auth/realms/master/protocol/openid-connect/token" | jq -r '.access_token')
``` 

You can thereafter access the API using the `Bearer <retrieved-token>` authentication scheme. The token has to be passed as an `Authorization` header.

## Running test
Before running test, make sure you have a running Keycloak server as explained above
JVM:
```bash
cd machi-todo-api
./mvnw clean test
```

NATIVE:
Native tests requires a connection to Postgres DB. You can indicate provide the datasource connection values as shown below.  
```bash
cd machi-todo-api
./mvnw clean install -Dnative -Dquarkus.datasource.url=<jdbc:postregs...> -Dquarkus.datasource.username=<pg-username> -Dquarkus.datasource.password=<pg-password>
```

## Development mode using in-memory H2 database
DevMode:
```bash
cd machi-todo-api
./mvnw compile quarkus:dev
```
Then, open: http://localhost:8080/swagger-ui

## Using PostgresSQL database
You'll need a running PostresSQL database to be able to ba able to use the application in `production`. 
By default the application will try to connect to PostgresSQL server running locally on the default port i.e `localhost:5432` and `todo-db` database. 
If you do not have a local running DB, use the following command `docker-compose up -f docker/backing-services.yml up db` to spin a PostgresSQL container.

NB: You can optionally provide a PostgreSql database url using the `QUARKUS_DATASOURCE_URL` env variable, database name via `QUARKUS_DATASOURCE_USERNAME` (default to `todo-api-user`) env variable and database password via `QUARKUS_DATASOURCE_PASSWORD` (default to `todo-api-pwd`) env variable. 
``` bash
export QUARKUS_DATASOURCE_URL=jdbc:postgresql://<pg-host-url>/<todo-db>
export QUARKUS_DATASOURCE_USERNAME=<username>
export QUARKUS_DATASOURCE_PASSWORD=<password>
```

Replace 
`<pg-host-url>`, `<todo-db>`,  `<username>` and `<password>` with custom values, or skip this step to use default values as explained above.
## Quick compilation and run on a JVM using PostgresSQL database

```bash
cd machi-todo-api
./mvnw package -DskipTests -DskipITs
java -jar machi-todo-api/target/machi-todo-api-1.0.0-SNAPSHOT-runner.jar
```

Then, open http://localhost:8080/swagger-ui

## Quick compilation and run on a JVM (in a container)
Compile:
```bash
cd machi-todo-api
./mvnw package -DskipTests -DskipITs
```

Run:
```bash
cd docker
docker-compose -f docker/docker-compose-jvm.yml up
```

NB: modify the `docker/docker.env` file so that it corresponds to real environment variables for `Keycloak` and `PostgresSQL`

## Compile to Native and run with PostgresSQL ( in a container )

Compile:
```bash
cd machi-todo-api
./mvnw package -Pnative -Dnative-image.docker-build=true
```

Run:
```bash
cd docker
docker-compose -f docker/docker-compose-native.yml up
```

NB: modify the `docker/docker.env` file so that it corresponds to real environment variables for `Keycloak` and `PostgresSQL`

## Health check

The api expose a readness check endpoint at `/health/ready` and a liveness check at `/health/live`
 

## Open API

OpenAPI schemas are exposed at `/openapi` 

### swagger 

To view the OpenAPi Swagger UI, go the following endpoint `/swagger-ui`

## Metrics

API metrics are exposed at `/metrics` 


# Features
To see the list of proposed endpoints visit the `/swagger-ui`
# HOW TO USE TODO API WITH QUARKUS OIDC
This is an example application based on a Todo API written using [Quarkus, a cloud native Java Stack](https://quarkus.io). The API permits users to perform basic operation on the TODO list i.e viewing created items,  creating new todo item, read, updating, or deleting from the database. Default this application uses an in-memory database called H2 in dev workflow and testing PostgreSQL in production.
  
## TODO Entity
A todo item is defined by:

An *id* - a unique identifier (also a primary key in our database)
By its *title* - `title` (required)
By the *owner* - `createdBy` - (required)
By its *priority* on the list - `order`
By its `status` - whether it is completed or still pending
By its `description`
  
**Note:** The `title` and `createdBy` forms a unique composite key which means that a user cannot create the same todo item more than once.

## List of endpoints

The API exposes the following list of endpoints:

**Health check:** The api expose a readiness check endpoint at `/health/ready` and a liveness check at `/health/live`

**Open API:** OpenAPI schemas are exposed at `/openapi` and the swagger documentation at `/swagger-ui`
  
**Metrics:** API metrics are exposed at `/metrics`

The application proposes a list of endpoints to manipulate the TODO items. To see the list of proposed endpoints visit the `/swagger-ui` swagger ui endpoints.

## Building and running the API.

### Prerequisite
The application is built on top of Quarkus. The following components are needed

1. Java programming language at least JDK8+. Make sure that JAVA_HOME environment variable is properly configured. 
2. Having [`maven`](https://maven.apache.org/download.cgi) at least **v3.5.3** installed. You can optionally use the maven wrapper provided in this repository - i.e the file `machi-todo-api/mvnw`. If you are on a window machine, use `machi-todo-api/mvnw.cmd` file.
3.  [Curl tool](https://curl.haxx.se/) - to make http request from the terminal
4.  [JQ tool](https://stedolan.github.io/jq/) to parse json (will be needed to parse Auth access token).
5.  [Keycloak](https://www.keycloak.org/), an OIDC compliant Auth server running locally - see instructions in the next section.
6.  [PostgreSQL](https://www.postgresql.org/) database running locally - see instructions below.
7. Make sure to have [Docker](https://www.docker.com/), and [docker-compose](https://docs.docker.com/compose/) installed. They are needed to launch the database and authentication server locally.
8. Git, to clone the project using 
```bash
git clone https://github.com/machi1990/todo-api-with-quarkus-oidc
```
Or optionally downloading a [zipped folder](`https://github.com/machi1990/todo-api-with-quarkus-oidc/archive/master.zip`)  

After you have cloned/downloaded the most recent version of the project, follow the following steps to have all the components installed and properly configured.

### OIDC Authentication with Keycloak
The application uses OpenID Connect for user authentication and authorization to protect the rest resources using bearer token authorization scheme.

The authorization tokens are issued by OpenId Connect and OAuth 2.0 compliant Authorization Servers such as Keycloak.

Bearer Token Authorization is the process of authorizing HTTP requests based on the existence and validity of a token which provides valuable information to determine the subject of the call as well whether or not an HTTP resource can be accessed depending

on user roles.

The application manages two types roles - `admin` and `users`. User with `admin` role can see all TODO items of all users, and they can delete all TODO items. Normal users i.e with a `users` role can create new TODO item, update and delete their items, complete an item, list and filter their items.

By default the application is configured to connect to a local (`localhost:8180`) running, an open source authentication server allowing single sign-on. You can optionally override authentication server url and the client id , and client secret by using providing

a full path (endpoint) to any other authentication `realm` from an authentication server that is OpenId Connect and OAuth 2.0 compliant.

See the example below:

```bash
export QUARKUS_OIDC_CLIENT_ID=<client-id>
export QUARKUS_OIDC_CREDENTIALS_SECRET=<client-secret>
export QUARKUS_OIDC_AUTH_SERVER_URL=http://<auth-server-url>/auth/realms/<realm-name>
```
See Quarkus OIDC security Guide for more information.

#### Populating Keycloak (creating realm and test users)
To launch the Keycloak server locally use the following command:

```bash
docker-compose -f docker/backing-services.yml up keycloak
```
The above command will start a Keycloak server with an empty users database. For testing and running our application locally we'll need to register our application (this consists of creating a client), authentication realm for our application

and populate the realm with few users. To do so, use the following command to create a java executable and launch it.

##### Building Keycloak realm creator project
```bash
cd keycloak-create-realm
```
Packaging the creator with maven installed in your machine

```bash
mvn clean install
```

Packaging the creator with maven wrapper located in `machi-todo-api` directory.

```bash
sudo chmod +ux ../machi-todo-api/mvnw
```

```bash
../machi-todo-api/mvnw clean install
```

Running the built jar file

```bash
java -jar target/keycloak-create-realm-1.0-SNAPSHOT-shaded.jar
```

Running the above command will create a `machi-todo-api` client_id with `secret` as the client_secret.

It will also create a `todo-api` realm and populate it with an admin user having the following credentials: username `admin` password `admin`.

A normal user i.e with `users` role will be created with the following credentials: username `machi`, password `admin`.

#### Retrieving an access token

You'll need to retrieve the access token from Keycloak auth server `https://localhost:8180/auth/realms/todo-api/protocol/openid-connect/token`.

The following curl commands should suffice  

**Admin access token:**

```bash
export admin_access_token=$(curl -X POST "http://localhost:8180/auth/realms/todo-api/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded" -d "client_id=machi-todo-api" -d "client_secret=secret" -d "username=admin" -d "password=admin" -d "grant_type=password" | jq -r '.access_token')
```

**Normal user access token:**

```bash
export users_access_token=$(curl -X POST "http://localhost:8180/auth/realms/todo-api/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded" -d "client_id=machi-todo-api" -d "client_secret=secret" -d "username=machi" -d "password=machi" -d "grant_type=password" | jq -r '.access_token')
```

Make sure to save the token somewhere, as they’ll be needed to access the API using the `Bearer <retrieved-token>` authentication scheme. The token has to be passed as an `Authorization` header.


### Starting a PostgresSQL database locally
We’ll need a running PostgreSQL DB to be able to persist our TODO items.

To spin up a new container we can use the following command:
```bash
docker-compose up -f docker/backing-services.yml up db
```

### Building and starting the API

Building the API requires running the following commands.
```bash
cd machi-todo-api
```

If you have maven 3.5.3 or above installed use the following command

```bash
mvn clean install -DskipTests -DskipITs
```

Otherwise, you can use the maven wrapper as shown below

```bash
sudo chmod +ux ./mvnw
```

```bash
./mvnw clean install -DskipTests -DskipITs
```

**Note:** We ignore tests; so that we can quickly build.

Once the build is finished, a runnable jar will be created. The jar can be executed using the following command.

```bash
java -jar machi-todo-api/target/machi-todo-api-1.0.0-SNAPSHOT-runner.jar
```

Then, open the browser to `http://localhost:8080/swagger-ui` to see the swagger UI showing the list of available endpoints.
 
## Features and how to interacting with the TODO rest endpoints
#### 1. Listing all todo items by an admin
An admin can see all todo items using the `GET /api/todos/admin` endpoint. Using the admin token we obtained above, we can make a curl request as follows:

```bash
curl -H "Authorization: Bearer $admin_access_token" localhost:8080/api/todos/admin
```

The list can be filtered by `createdBy` ie. the owner of the item and `status` query params, the later being an enum of two possible values i.e `completed` and `pending`

#### 2. Delete all TODO items by an admin:
An admin can delete all todo items using the `DELETE /api/todos/admin` endpoint

```bash
curl -X DELETE -H "Authorization: Bearer $admin_access_token" localhost:8080/api/todos/admin
```

#### 3. Listing users TODO items:
TODO list is available at the `GET /api/todos` endpoint. Using the user token we obtained above, we can make an http request to the api as follows:  

```bash
curl -H "Authorization: Bearer $users_access_token" localhost:8080/api/todos
```

The list can be filtered by `status` query param which is an enum of two possible values i.e `completed` and `pending`

#### 4. Delete all users completed TODO items :
A user can delete all todo items that have been completed using the `DELETE /api/todos`

```bash
curl -X DELETE -H "Authorization: Bearer $users_access_token" localhost:8080/api/todos
```

#### 5. Delete user’s TODO item by id:

A user can delete a given todo item (irrespective of the status) using the `DELETE /api/todos/{id}` endpoint

```bash
curl -X DELETE -H "Authorization: Bearer $users_access_token" localhost:8080/api/todos/1
```

#### 6. GET user’s TODO item by id:

A user can retrieve a given todo item (irrespective of the status) by its id using the `GET /api/todos/{id}` endpoint

```bash
curl -H "Authorization: Bearer $users_access_token" localhost:8080/api/todos/1
```

#### 7. Creating a user todo item:
A user can create a TODO item using the `POST /api/todos` endpoint. The endpoint will return the `id` of the created todo item.

```bash
curl -X POST -H "Authorization: Bearer $users_access_token" -H "Content-Type: application/json" -d '{"title":"first","description":"description","order":0}' localhost:8080/api/todos/
```

#### 8. Updating a user todo item:
A user can update, i.e change the status, the title, the description, the order of a TODO item using the `PUT /api/todos/{id}` endpoint. The endpoint will return the `id` of the created todo item.
```bash
curl -X PUT -H "Authorization: Bearer $users_access_token" -H "Content-Type: application/json" -d '{"title":"first","description":"description","order":0, "status":"completed"}' localhost:8080/api/todos/1
```

## A note on Authentication:
A user with admin role cannot access `/api/todos` rest resource which is only restricted to users. Inversely, a user does not have access to `/api/todos/admin` rest resource.

Example:
Using the admin access token to make a curl request to `/api/todos`. For example,
```bash
curl -I -H "Authorization: Bearer $admin_access_token" localhost:8080/api/todos
```
will display `HTPP 1.0 403` on your terminal indicating that an admin user is forbidden to access the resource.

If we try to access access the api without an authorization header it will return an http error indicating missing authentication. For example,

```bash
curl -I localhost:8080/api/todos
```
will display `HTPP 1.0 401` on your terminal indicating that we are not authentified to access the API.

## Conclusion and going in production.

The API is container ready and cloud native. Two dockerfiles are provided, one for normal JVM run and another one for a Native Image (native executable) run.

Instructions on how to build each respective docker images are available in the docker files.

NB: When running the container in production, make sure to provide production Auth Server and datasource environment variables. Mandatory variables are shown in the [`docker/docker.env`](https://github.com/machi1990/todo-api-with-quarkus-oidc/blob/master/docker/docker.env) file in the project git repository.

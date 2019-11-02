
# Quickstart (Building and running the API.)

## Prerequisite
1. Java programming language at least JDK8+. Make sure that JAVA_HOME environment variable is properly configured.

2. Having [`maven`](https://maven.apache.org/download.cgi) at least **v3.5.3** installed. You can optionally use the maven wrapper provided in this repository - i.e the file `machi-todo-api/mvnw`. If you are on a window machine, use `machi-todo-api/mvnw.cmd` file.

3.  [Curl tool](https://curl.haxx.se/) - to make http request from the terminal

4.  [JQ tool](https://stedolan.github.io/jq/) to parse json (will be needed to parse Auth access token).

5.  [Keycloak](https://www.keycloak.org/), an OIDC compliant Auth server running locally - see instructions in the next section.

6.  [PostgreSQL](https://www.postgresql.org/) database running locally - see instructions below.

7. Make sure to have [Docker](https://www.docker.com/), and [docker-compose](https://docs.docker.com/compose/) installed. They are needed to launch the database and authentication server locally.


We'll need three terminal tabs, one to start the application and two others to start external services i.e Keycloak and PostgresSQL database.

## 1. Cloning the project
Open the terminal and run the following command.
```bash
git clone https://github.com/machi1990/todo-api-with-quarkus-oidc
```  
 
## 2. Starting  Keycloak  server
From a new terminal tab, run the following command.

```bash
cd todo-api-with-quarkus-oidc
```

```bash
docker-compose -f docker/backing-services.yml up keycloak
```
## 3. Starting a PostgresSQL database locally
From a another terminal tab, run the following command
```bash
docker-compose up -f docker/backing-services.yml up db
```

## 4.  Populating Keycloak
This command can be run on our first terminal (where we cloned the project) 

Let's cd into the `keycloak-create-realm` module of our cloned project.
```bash
cd todo-api-with-quarkus-oidc/keycloak-create-realm
```

We'll have to package the module using maven by running the following command
```bash
mvn clean install
```

Or if maven is not installed we can package the module creator with maven wrapper located in `machi-todo-api` directory.
```bash
sudo chmod +ux ../machi-todo-api/mvnw
```

```bash
../machi-todo-api/mvnw clean install
```

Running the built jar file. 
**Note** Make sure that Keycloak server is properly started from step 2 above before running the command below.
 
```bash
java -jar target/keycloak-create-realm-1.0-SNAPSHOT-shaded.jar
```

## 5. Building and starting the API

After having ran step 4 successfully, we'll now have to build the API using the following commands (to be run in the same terminal as step 4).

```bash
cd ../machi-todo-api
```

If you have maven 3.5.3 or above installed use the following command  
```bash
mvn clean install -DskipTests -DskipITs
```

Otherwise, you can use the maven wrapper as shown below 
```bash
./mvnw clean install -DskipTests -DskipITs
```

**Note:** We ignore tests; so that we can quickly build.

Once the build is finished, a runnable jar will be created. The jar can be executed using the following command.

 ```bash
java -jar machi-todo-api/target/machi-todo-api-1.0.0-SNAPSHOT-runner.jar
```

This will start the application on `localhost:8080`. Opening the browser to  `http://localhost:8080/swagger-ui` will open a swagger ui listing all API's endpoints.  
 

## 6.  Features and how to interacting with the TODO rest endpoints

We'll need a bearer access token to interact with the API using the `Bearer <retrieved-token>` authentication scheme. The token has to be passed as an `Authorization` header.

### User TODO resource
We'll need to retrieve the **users** role access token use the following command before interacting with the `/api/todos/` endpoints.

```bash
export users_access_token=$(curl -X POST "http://localhost:8180/auth/realms/todo-api/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded" -d "client_id=machi-todo-api" -d "client_secret=secret" -d "username=machi" -d "password=machi" -d "grant_type=password" | jq -r '.access_token')
```
#### 1. Listing users TODO items:
TODO list is available at the `GET /api/todos` endpoint. Using the user token we obtained above, we can make an http request to the api as follows:

```bash
curl -H "Authorization: Bearer $users_access_token" localhost:8080/api/todos
```

The list can be filtered by `status` query param which is an enum of two possible values i.e `completed` and `pending`

#### 2. Delete all users completed TODO items :
A user can delete all todo items that have been completed using the `DELETE /api/todos`

```bash
curl -X DELETE -H "Authorization: Bearer $users_access_token" localhost:8080/api/todos
```

#### 3. Delete user’s TODO item by id:
A user can delete a given todo item (irrespective of the status) using the `DELETE /api/todos/{id}` endpoint
```bash
curl -X DELETE -H "Authorization: Bearer $users_access_token" localhost:8080/api/todos/1
```

#### 4. GET user’s TODO item by id:
A user can retrieve a given todo item (irrespective of the status) by its id using the `GET /api/todos/{id}` endpoint

```bash
curl -H "Authorization: Bearer $users_access_token" localhost:8080/api/todos/1
```

#### 5. Creating a user todo item:
A user can create a TODO item using the `POST /api/todos` endpoint. The endpoint will return the `id` of the created todo item.

```bash
curl -X POST -H "Authorization: Bearer $users_access_token" -H "Content-Type: application/json" -d '{"title":"first","description":"description","order":0}' localhost:8080/api/todos/
```

#### 6. Updating a user todo item:

A user can update, i.e change the status, the title, the description, the order of a TODO item using the `PUT /api/todos/{id}` endpoint. The endpoint will return the `id` of the created todo item.

```bash
curl -X PUT -H "Authorization: Bearer $users_access_token" -H "Content-Type: application/json" -d '{"title":"first","description":"description","order":0, "status":"completed"}' localhost:8080/api/todos/1
```

### Admin TODO resource
We'll need to retrieve the **admin**  role access token use the following command before interacting with the `/api/todos/admin` endpoints.
```bash
export admin_access_token=$(curl -X POST "http://localhost:8180/auth/realms/todo-api/protocol/openid-connect/token" -H "Content-Type: application/x-www-form-urlencoded" -d "client_id=machi-todo-api" -d "client_secret=secret" -d "username=admin" -d "password=admin" -d "grant_type=password" | jq -r '.access_token')
```
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

### A note on Authentication:

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
  

## Going in production.
The API is container ready and cloud native. Two dockerfiles are provided, one for normal JVM run and another one for a Native Image (native executable) run.

Instructions on how to build each respective docker images are available in the docker files.

**Note:** When running the container in production, make sure to provide production Auth Server and datasource environment variables. Mandatory variables are shown in the [`docker/docker.env`](https://github.com/machi1990/todo-api-with-quarkus-oidc/blob/master/docker/docker.env) file in the project git repository.

package org.machi.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.machi.keycloak.KeycloakCreateRealm.getAccessToken;

@QuarkusTest
@QuarkusTestResource(KeycloakTestResource.class)
public class UserTodoResourceTest {
    public final String MACHI_ACCESS_TOKEN = getAccessToken(KeycloakTestResource.MACHI_USER);
    public final String ANOTHER_USER_ACCESS_TOKEN = getAccessToken(KeycloakTestResource.ANOTHER_USER);
    public final String ADMIN_ACCESS_TOKEN = getAccessToken(KeycloakTestResource.ADMIN);

    @AfterEach
    public void cleanAll(){
        given()
                .when()
                .auth().oauth2(ADMIN_ACCESS_TOKEN)
                .delete("/api/todos/admin")
                .then()
                .statusCode(204);
    }

    @Test
    public void assertEmptyTodoList() {
        given()
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .get("/api/todos")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(0));
    }

    @Test
    public void retrievingUnExistingTodoShouldReturn404() {
        given()
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .get("/api/todos/89876565")
                .then()
                .statusCode(404)
                .assertThat();
    }

    @Test
    public void creatingTheSameTodoTwiceShouldThrowAnError() {
        String todo = "{\"title\":\"todo-title\",\"description\":\"todo-description\",\"order\":1}";
        given().
                contentType(ContentType.JSON)
                .body(todo)
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .post("/api/todos")
                .then()
                .statusCode(201)
                .extract().body().asString();

        given().
                contentType(ContentType.JSON)
                .body(todo)
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .post("/api/todos")
                .then()
                .statusCode(CoreMatchers.not(is(201)))
                .extract().body().asString();
    }

    @Test
    @DisplayName("Test retrieving a list of my TODOs")
    public void todoListIsNotEmpty() {
        String todo = "{\"title\":\"todo-title\",\"description\":\"todo-description\",\"order\":1}";

        String createdId = given().
                contentType(ContentType.JSON)
                .body(todo)
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .post("/api/todos")
                .then()
                .statusCode(201)
                .extract().body().asString();

        // retrieving created todo
        given()
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .get("/api/todos/"+createdId)
                .then()
                .statusCode(200)
                .assertThat()
                .body("title", is("todo-title"))
                .body("createdBy", is(KeycloakTestResource.MACHI_USER))
                .body("order", is(1))
                .body("status", is("pending"))
                .body("description", is("todo-description"));

        // access the list of todos
        given()
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .get("/api/todos")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(1))
                .body("[0].title", is("todo-title"))
                .body("[0].order", is(1))
                .body("[0].description", is("todo-description"));


        // verify that another user does not have access to my todo list
        given()
                .when()
                .auth().oauth2(ANOTHER_USER_ACCESS_TOKEN)
                .get("/api/todos")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(0));

        given()
                .when()
                .auth().oauth2(ANOTHER_USER_ACCESS_TOKEN)
                .get("/api/todos/"+createdId)
                .then()
                .statusCode(404)
                .assertThat();


        // updating my todo
        String editedTodo = "{\"title\":\"todo-title\",\"description\":\"revisited-todo-description\",\"order\":1}";
        given().contentType(ContentType.JSON)
                .body(editedTodo)
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .put("/api/todos/"+createdId)
                .then()
                .statusCode(200)
                .body("description",is("revisited-todo-description"));

        // delete TODO
        given()
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .delete("/api/todos/"+createdId)
                .then()
                .statusCode(204);

        // check that all todos have been deleted
        assertEmptyTodoList();

        // created another todo
        String anotherTodo = "{\"title\":\"todo-title\",\"description\":\"revisited-todo-description\",\"order\":1}";
        String anotherCreatedId = given().
                contentType(ContentType.JSON)
                .body(anotherTodo)
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .post("/api/todos")
                .then()
                .statusCode(201)
                .extract().body().asString();

        // complete my todo
        editedTodo = "{\"title\":\"todo-title\",\"description\":\"revisited-todo-description\",\"order\":1,\"status\":\"completed\"}";
        given().contentType(ContentType.JSON)
                .body(editedTodo)
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .put("/api/todos/"+anotherCreatedId)
                .then()
                .statusCode(200)
                .body("status",is("completed"));

        // delete all completed TODO
        given()
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .delete("/api/todos")
                .then()
                .statusCode(204);

        // check that all I have an empty list of todo items
        assertEmptyTodoList();
    }


    @Test
    public void filteringByStatus() {
        String todo = "{\"title\":\"todo-title\",\"description\":\"todo-description\",\"order\":1}";
        given().
                contentType(ContentType.JSON)
                .body(todo)
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .post("/api/todos")
                .then()
                .statusCode(201)
                .extract().body().asString();

       todo = "{\"title\":\"second-todo-title\",\"description\":\"todo-description\",\"order\":1}";
        String secondCreated = given().
                contentType(ContentType.JSON)
                .body(todo)
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .post("/api/todos")
                .then()
                .statusCode(is(201))
                .extract().body().asString();


        // should return empty list when filtering completed jobs
        given()
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .queryParam("status", "completed")
                .get("/api/todos")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(0));

        // pending items should be 2
        given()
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .queryParam("status", "pending")
                .get("/api/todos")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(2));

        // complete one todo
        String editedTodo = "{\"title\":\"second-todo-title\",\"description\":\"revisited-todo-description\",\"order\":1,\"status\":\"completed\"}";
        given().contentType(ContentType.JSON)
                .body(editedTodo)
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .put("/api/todos/"+secondCreated)
                .then()
                .statusCode(200)
                .body("status",is("completed"));

        // should return empty list when filtering completed jobs
        given()
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .queryParam("status", "completed")
                .get("/api/todos")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(1));

        // should return empty list when filtering completed jobs
        given()
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .queryParam("status", "pending")
                .get("/api/todos")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(1));

    }

}
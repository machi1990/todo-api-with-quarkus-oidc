package org.machi.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.machi.keycloak.KeycloakCreateRealm.getAccessToken;

@QuarkusTest
@QuarkusTestResource(KeycloakTestResource.class)
public class AdminResourceTest {
    public final String ADMIN_ACCESS_TOKEN = getAccessToken(KeycloakTestResource.ADMIN);
    public static final String MACHI_ACCESS_TOKEN = getAccessToken(KeycloakTestResource.MACHI_USER);

    @AfterEach
    public void cleanAll() {
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
                .auth().oauth2(ADMIN_ACCESS_TOKEN)
                .get("/api/todos/admin")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(0));
    }

    @Test
    @DisplayName("Test retrieving a list of TODOs and deleting them all using admin access token")
    public void todoListIsNotEmpty() {
        String todo = "{\"title\":\"first\",\"description\":\"description\",\"order\":0}";

        given().
                contentType(ContentType.JSON)
                .body(todo)
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .post("/api/todos")
                .then()
                .statusCode(201);


        String accessToken = getAccessToken(KeycloakTestResource.ADMIN);

        // access the list of todos
        given()
                .when()
                .auth().oauth2(accessToken)
                .get("/api/todos/admin")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(1))
                .body("[0].title", is("first"))
                .body("[0].order", is(0))
                .body("[0].description", is("description"));

        // Deleting all TODO using admin rights
        given()
                .when()
                .auth().oauth2(accessToken)
                .delete("/api/todos/admin")
                .then()
                .statusCode(204);

        // check that all todos have been deleted
        assertEmptyTodoList();
    }

    @Test
    public void filteringByCreatedByAndStatus() {
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

        given().
                contentType(ContentType.JSON)
                .body(todo)
                .when()
                .auth().oauth2(MACHI_ACCESS_TOKEN)
                .post("/api/todos")
                .then()
                .statusCode(is(201))
                .extract().body().asString();

        // machi user has two todo lists
        given()
                .when()
                .auth().oauth2(ADMIN_ACCESS_TOKEN)
                .queryParam("createdBy", KeycloakTestResource.MACHI_USER)
                .get("/api/todos/admin")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(2));

        // another user has no todo lists
        given()
                .when()
                .auth().oauth2(ADMIN_ACCESS_TOKEN)
                .queryParam("createdBy", KeycloakTestResource.ANOTHER_USER)
                .get("/api/todos/admin")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(0));

        // filtering by machi and completed status should return an empty list
        given()
                .when()
                .auth().oauth2(ADMIN_ACCESS_TOKEN)
                .queryParam("status", "completed")
                .queryParam("createdBy", KeycloakTestResource.MACHI_USER)
                .get("/api/todos/admin")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(0));

        // filtering by machi and pending status should return two created todo items
        given()
                .when()
                .auth().oauth2(ADMIN_ACCESS_TOKEN)
                .queryParam("status", "pending")
                .queryParam("createdBy", KeycloakTestResource.MACHI_USER)
                .get("/api/todos/admin")
                .then()
                .statusCode(200)
                .assertThat()
                .body("size()", is(2));
    }
}

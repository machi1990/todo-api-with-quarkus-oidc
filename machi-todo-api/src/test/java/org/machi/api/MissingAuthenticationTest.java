package org.machi.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.machi.keycloak.KeycloakCreateRealm.*;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(KeycloakTestResource.class)
public class MissingAuthenticationTest {
    @Test
    public void testMissingAuthentication() {
        given()
                .when().get("/api/todos/admin")
                .then()
                .statusCode(401);

        given()
                .when().get("/api/todos")
                .then()
                .statusCode(401);
    }

    @Test
    public void testAccessToAdminResourceByUser() {
        given().auth().oauth2(getAccessToken(KeycloakTestResource.MACHI_USER))
                .when().get("/api/todos/admin")
                .then()
                .statusCode(403);
    }

    @Test
    public void testAccessToUserResourceByAdmin() {
        given().auth().oauth2(getAccessToken(KeycloakTestResource.ADMIN))
                .when().get("/api/todos")
                .then()
                .statusCode(403);
    }
}

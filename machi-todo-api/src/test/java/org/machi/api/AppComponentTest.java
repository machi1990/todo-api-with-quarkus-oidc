package org.machi.api;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class AppComponentTest {
    @Test
    public void shouldHaveSwaggerEndpoint() {
        given()
                .when().get("/swagger-ui")
                .then()
                .statusCode(200);
    }

    @Test
    public void shouldHaveOpenApiEndpoint() {
        given()
                .when().get("/openapi")
                .then()
                .statusCode(200);
    }

    @Test
    public void shouldHaveMetricsEndpoint() {
        given()
                .when().get("/metrics")
                .then()
                .statusCode(200);
    }


    @Test
    public void shouldHaveHealthCheckEndpoints() {
        given()
                .when().get("/health")
                .then()
                .statusCode(200);

        given()
                .when().get("/health/live")
                .then()
                .statusCode(200);

        given()
                .when().get("/health/ready")
                .then()
                .statusCode(200);
    }

}

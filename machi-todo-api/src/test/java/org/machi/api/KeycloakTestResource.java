package org.machi.api;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.keycloak.representations.idm.RealmRepresentation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.machi.keycloak.KeycloakCreateRealm.*;

public class KeycloakTestResource implements QuarkusTestResourceLifecycleManager {
    public static final String ADMIN = "admin";
    public static final String ANOTHER_USER = "other";
    public static final String MACHI_USER = "machi";

    @Override
    public Map<String, String> start() {
        HashMap<String, String> map = new HashMap<>();
        map.put("keycloak.url", System.getProperty("keycloak.url"));

        RealmRepresentation realm = createRealm(KEYCLOAK_REALM);

        realm.getClients().add(createClient(KEYCLOAK_CLIENT_ID));
        realm.getUsers().add(createUser(MACHI_USER, "user"));
        realm.getUsers().add(createUser(ANOTHER_USER, "user"));
        realm.getUsers().add(createUser(ADMIN, "admin"));
        try {
            configureKeycloakRealm(realm);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public void stop() {
        given()
                .auth().oauth2(getAdminAccessToken())
                .when()
                .delete(KEYCLOAK_SERVER_URL + "/admin/realms/" + KEYCLOAK_REALM).then().statusCode(204);
    }
}

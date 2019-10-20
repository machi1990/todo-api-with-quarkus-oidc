package org.machi.keycloak;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.idm.authorization.PolicyRepresentation;
import org.keycloak.representations.idm.authorization.ResourceRepresentation;
import org.keycloak.representations.idm.authorization.ResourceServerRepresentation;
import org.keycloak.util.JsonSerialization;

import io.restassured.RestAssured;

public class KeycloakCreateRealm {
    public static final String KEYCLOAK_SERVER_URL = System.getProperty("keycloak.url", "http://localhost:8180/auth");
    public static final String KEYCLOAK_REALM = System.getProperty("keycloak.realm", "todo-api");
    public static final String KEYCLOAK_CLIENT_ID = System.getProperty("keycloak.clientid", "machi-todo-api");

    public static void main(String[] args) throws IOException {
        RealmRepresentation realm = createRealm(KEYCLOAK_REALM);

        realm.getClients().add(createClient(KEYCLOAK_CLIENT_ID));
        realm.getUsers().add(createUser("machi", "user"));
        realm.getUsers().add(createUser("admin", "user", "admin"));
        configureKeycloakRealm(realm);
    }

    public static void configureKeycloakRealm(RealmRepresentation realm) throws IOException {
        RestAssured
                .given()
                .auth().oauth2(getAdminAccessToken())
                .contentType("application/json")
                .body(JsonSerialization.writeValueAsBytes(realm))
                .when()
                .post(KEYCLOAK_SERVER_URL + "/admin/realms").then()
                .statusCode(201);
    }

    public static void removeKeycloakRealm() {
        RestAssured
                .given()
                .auth().oauth2(getAdminAccessToken())
                .when()
                .delete(KEYCLOAK_SERVER_URL + "/admin/realms/" + KEYCLOAK_REALM).then().statusCode(204);
    }

    public static String getAdminAccessToken() {
        return RestAssured
                .given()
                .param("grant_type", "password")
                .param("username", "admin")
                .param("password", "admin")
                .param("client_id", "admin-cli")
                .when()
                .post(KEYCLOAK_SERVER_URL + "/realms/master/protocol/openid-connect/token")
                .as(AccessTokenResponse.class).getToken();
    }

    public static RealmRepresentation createRealm(String name) {
        RealmRepresentation realm = new RealmRepresentation();

        realm.setRealm(name);
        realm.setEnabled(true);
        realm.setUsers(new ArrayList<>());
        realm.setClients(new ArrayList<>());

        RolesRepresentation roles = new RolesRepresentation();
        List<RoleRepresentation> realmRoles = new ArrayList<>();

        roles.setRealm(realmRoles);
        realm.setRoles(roles);

        realm.getRoles().getRealm().add(new RoleRepresentation("user", null, false));
        realm.getRoles().getRealm().add(new RoleRepresentation("admin", null, false));

        return realm;
    }

    public static ClientRepresentation createClient(String clientId) {
        ClientRepresentation client = new ClientRepresentation();

        client.setClientId(clientId);
        client.setPublicClient(false);
        client.setSecret("secret");
        client.setDirectAccessGrantsEnabled(true);
        client.setEnabled(true);

        client.setAuthorizationServicesEnabled(true);

        ResourceServerRepresentation authorizationSettings = new ResourceServerRepresentation();

        authorizationSettings.setResources(new ArrayList<>());
        authorizationSettings.setPolicies(new ArrayList<>());

        configureConfidentialResourcePermission(authorizationSettings);
        configurePermissionResourcePermission(authorizationSettings);

        client.setAuthorizationSettings(authorizationSettings);

        return client;
    }

    public static void configureConfidentialResourcePermission(ResourceServerRepresentation authorizationSettings) {
        ResourceRepresentation resource = new ResourceRepresentation("Confidential Resource");

        resource.setUris(Collections.singleton("/api/confidential"));

        authorizationSettings.getResources().add(resource);

        PolicyRepresentation policy = new PolicyRepresentation();

        policy.setName("Confidential Policy");
        policy.setType("js");
        policy.setConfig(new HashMap<>());
        policy.getConfig().put("code",
                "var identity = $evaluation.context.identity;\n" +
                        "\n" +
                        "if (identity.hasRealmRole(\"confidential\")) {\n" +
                        "$evaluation.grant();\n" +
                        "}");

        authorizationSettings.getPolicies().add(policy);

        PolicyRepresentation permission = new PolicyRepresentation();

        permission.setName("Confidential Permission");
        permission.setType("resource");
        permission.setResources(new HashSet<>());
        permission.getResources().add(resource.getName());
        permission.setPolicies(new HashSet<>());
        permission.getPolicies().add(policy.getName());

        authorizationSettings.getPolicies().add(permission);
    }

    public static void configurePermissionResourcePermission(ResourceServerRepresentation authorizationSettings) {
        ResourceRepresentation resource = new ResourceRepresentation("Permission Resource");

        resource.setUris(Collections.singleton("/api/permission"));

        authorizationSettings.getResources().add(resource);

        PolicyRepresentation policy = new PolicyRepresentation();

        policy.setName("Permission Policy");
        policy.setType("js");
        policy.setConfig(new HashMap<>());
        policy.getConfig().put("code", "$evaluation.grant();");

        authorizationSettings.getPolicies().add(policy);

        PolicyRepresentation permission = new PolicyRepresentation();

        permission.setName("Permission Resource Permission");
        permission.setType("resource");
        permission.setResources(new HashSet<>());
        permission.getResources().add(resource.getName());
        permission.setPolicies(new HashSet<>());
        permission.getPolicies().add(policy.getName());

        authorizationSettings.getPolicies().add(permission);
    }

    public static UserRepresentation createUser(String username, String... realmRoles) {
        UserRepresentation user = new UserRepresentation();

        user.setUsername(username);
        user.setEnabled(true);
        user.setCredentials(new ArrayList<>());
        user.setRealmRoles(Arrays.asList(realmRoles));

        CredentialRepresentation credential = new CredentialRepresentation();

        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(username);
        credential.setTemporary(false);

        user.getCredentials().add(credential);

        return user;
    }


    public static String getAccessToken(String userName) {
        return RestAssured
                .given()
                .param("grant_type", "password")
                .param("username", userName)
                .param("password", userName)
                .param("client_id", KEYCLOAK_CLIENT_ID)
                .param("client_secret", "secret")
                .when()
                .post(KEYCLOAK_SERVER_URL + "/realms/" + KEYCLOAK_REALM + "/protocol/openid-connect/token")
                .as(AccessTokenResponse.class).getToken();
    }
}

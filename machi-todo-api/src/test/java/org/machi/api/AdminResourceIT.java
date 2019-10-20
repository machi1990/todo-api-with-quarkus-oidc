package org.machi.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(KeycloakTestResource.class)
public class AdminResourceIT extends AdminResourceTest {
}

package org.machi.api;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.SubstrateTest;

@SubstrateTest
@QuarkusTestResource(KeycloakTestResource.class)
public class UserTodoResourceIT extends UserTodoResourceTest {

    // Execute the same tests but in native mode.
}
package org.machi.api;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api")
@OpenAPIDefinition(info = @Info(
        title = "TODO API With Quarkus OIDC",
        version = "1.0.0", contact = @Contact(
                name = "Manyanda Chitimbo",
        email = "manyanda.chitimbo@gmail.com"
)))
public class TodoApplication extends Application {
}

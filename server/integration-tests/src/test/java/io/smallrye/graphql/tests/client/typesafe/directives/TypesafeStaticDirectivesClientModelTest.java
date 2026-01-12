package io.smallrye.graphql.tests.client.typesafe.directives;

import static io.smallrye.graphql.client.modelbuilder.ClientModelBuilder.build;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.eclipse.microprofile.graphql.Name;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.jandex.Index;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.tests.client.typesafe.directives.model.SomeClassClient;
import io.smallrye.graphql.tests.client.typesafe.directives.model.SomeClassServer;

@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeStaticDirectivesClientModelTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "typesafe-directive-client-model.war")
                .addClasses(SomeClassServer.class, ServerApi.class,
                        // needed for the server-side (graphql-java) validation
                        FieldDirective.class,
                        VariableDefinitionDirective.class);
    }

    @ArquillianResource
    URL testingURL;

    protected ClientApi client;
    private boolean onlyOnce = false;

    @Before
    public void prepare() {
        if (!onlyOnce) {
            Index index = null;
            try {
                index = Index.of(SomeClassClient.class, ClientApi.class,
                        Name.class,
                        FieldDirective.class,
                        FieldDirective.FieldDirectives.class,
                        VariableDefinitionDirective.class,
                        VariableDefinitionDirective.VariableDefinitionDirectives.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            client = new VertxTypesafeGraphQLClientBuilder()
                    .clientModels(build(index))
                    .endpoint(testingURL.toString() + "graphql")
                    .build(ClientApi.class);
        }
    }

    @Test
    public void singleQueryDirectiveTest() {
        final SomeClassClient queryInput = new SomeClassClient("a", 1);
        // query checking is on the server side API
        final SomeClassClient queryResult = client.getQuerySomeClass(queryInput, false);
        assertEquals(queryInput.getId(), queryResult.getId());
        assertEquals(queryInput.getNumber(), queryResult.getNumber());
    }
}

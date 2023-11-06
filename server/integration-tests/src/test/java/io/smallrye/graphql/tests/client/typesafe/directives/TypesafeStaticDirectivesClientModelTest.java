package io.smallrye.graphql.tests.client.typesafe.directives;

import static io.smallrye.graphql.client.model.ClientModelBuilder.build;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URL;

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

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.vertx.typesafe.VertxTypesafeGraphQLClientBuilder;
import io.smallrye.graphql.tests.client.typesafe.directives.model.SomeClass;

@RunWith(Arquillian.class)
@RunAsClient
public class TypesafeStaticDirectivesClientModelTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "typesafe-directive-client-model.war")
                .addClasses(SomeClass.class, ServerApi.class,
                        // needed for the server-side (java-graphql) validation
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
                index = Index.of(SomeClass.class, ClientApi.class, ServerApi.class,
                        FieldDirective.class,
                        FieldDirective.FieldDirectives.class,
                        VariableDefinitionDirective.class,
                        VariableDefinitionDirective.VariableDefinitionDirectives.class,
                        GraphQLClientApi.class);
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
        SomeClass queryInput = new SomeClass("a", 1);
        // query checking is on the server side API
        assertEquals(queryInput, client.getQuerySomeClass(queryInput, false));
    }

}

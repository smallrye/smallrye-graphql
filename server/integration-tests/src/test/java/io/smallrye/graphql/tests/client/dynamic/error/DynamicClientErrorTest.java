package io.smallrye.graphql.tests.client.dynamic.error;

import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.impl.ResponseImpl;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;

/**
 * Test error handling features of the dynamic client
 */
@ExtendWith(ArquillianExtension.class)
@RunAsClient
public class DynamicClientErrorTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(ErrorApi.class, Dummy.class);
    }

    @ArquillianResource
    URL testingURL;

    private static VertxDynamicGraphQLClient client;

    @BeforeEach
    public void prepare() {
        client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .build();
    }

    /**
     * Test the `ResponseImpl.throwExceptionIfErrors` method that should throw a `GraphQLClientException` if the Response
     * contains any errors.
     */
    @Test
    public void convertToGraphQLClientException() throws ExecutionException, InterruptedException {
        Response response = client.executeSync(document(operation(field("dummy", field("foo"), field("bar")))));
        try {
            ((ResponseImpl) response).throwExceptionIfErrors();
            fail("`throwExceptionIfErrors` call should throw a GraphQLClientException");
        } catch (GraphQLClientException e) {
            assertArrayEquals(new Object[] { "dummy", "bar" }, e.getErrors().get(0).getPath());
        }
    }

    @GraphQLApi
    public static class ErrorApi {

        @Query
        public Dummy dummy() {
            Dummy dummy = new Dummy();
            dummy.setFoo(5);
            return dummy;
        }

        public Integer bar(@Source Dummy dummy) {
            throw new RuntimeException("asdf");
        }

    }

    public static class Dummy {

        private Integer foo;

        public Integer getFoo() {
            return foo;
        }

        public void setFoo(Integer foo) {
            this.foo = foo;
        }
    }

}

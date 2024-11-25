package io.smallrye.graphql.tests.client.dynamic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;

/**
 * Verify that dynamic clients can parse Java records from responses
 */
@RunWith(Arquillian.class)
@RunAsClient
public class RecordAsInputToDynamicClientTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(SimpleRecord.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void testSimpleRecord() throws Exception {
        try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql").build()) {
            Response response = client.executeSync("query { simple {a b} }");
            SimpleRecord result = response.getObject(SimpleRecord.class, "simple");
            assertEquals("a", result.a());
            assertEquals("b", result.b());
        }
    }

    /**
     * Try selecting only a subset of fields supported by the record.
     */
    @Test
    public void testPartial() throws Exception {
        try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql").build()) {
            Response response = client.executeSync("query { simple {a} }");
            SimpleRecord result = response.getObject(SimpleRecord.class, "simple");
            assertEquals("a", result.a());
            assertNull(result.b());
        }

    }

    /**
     * Just to be sure that if I reverse the order of fields in the query,
     * they won't get mixed up on the client side.
     */
    @Test
    public void testReversed() throws Exception {
        try (DynamicGraphQLClient client = new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql").build()) {
            Response response = client.executeSync("query { simple {b a} }");
            SimpleRecord result = response.getObject(SimpleRecord.class, "simple");
            assertEquals("a", result.a());
            assertEquals("b", result.b());
        }

    }

    @GraphQLApi
    public static class Api {

        @Query
        public SimpleRecord simple() {
            return new SimpleRecord("a", "b");
        }

    }

    public record SimpleRecord(String a, String b) {
    }

}

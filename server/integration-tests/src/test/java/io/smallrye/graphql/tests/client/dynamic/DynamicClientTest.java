package io.smallrye.graphql.tests.client.dynamic;

import static io.smallrye.graphql.client.mpapi.core.Argument.arg;
import static io.smallrye.graphql.client.mpapi.core.Argument.args;
import static io.smallrye.graphql.client.mpapi.core.Document.document;
import static io.smallrye.graphql.client.mpapi.core.Field.field;
import static io.smallrye.graphql.client.mpapi.core.Operation.operation;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

import javax.json.JsonObject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClientBuilder;
import io.smallrye.graphql.client.mpapi.Response;
import io.smallrye.graphql.client.mpapi.core.Document;

@RunWith(Arquillian.class)
@RunAsClient
public class DynamicClientTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "validation-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(DynamicClientApi.class, Dummy.class);
    }

    @ArquillianResource
    URL testingURL;

    private static VertxDynamicGraphQLClient client;

    @Before
    public void prepare() {
        client = (VertxDynamicGraphQLClient) new VertxDynamicGraphQLClientBuilder()
                .url(testingURL.toString() + "graphql")
                .build();
    }

    @After
    public void cleanup() {
        client.close();
    }

    @Test
    public void testOneQueryInOneOperationSync() throws ExecutionException, InterruptedException {
        Document document = document(
                operation("SimpleQuery",
                        field("simple",
                                field("string"),
                                field("integer"))));
        JsonObject data = client.executeSync(document).getData();
        assertEquals("asdf", data.getJsonObject("simple").getString("string"));
        assertEquals(30, data.getJsonObject("simple").getInt("integer"));
    }

    @Test
    public void testTwoQueriesInOneOperationSync() throws ExecutionException, InterruptedException {
        Document document = document(
                operation("SimpleQuery",
                        field("simple",
                                field("string"),
                                field("integer")),
                        field("simple2",
                                field("string"),
                                field("integer"))));
        JsonObject data = client.executeSync(document).getData();
        assertEquals("asdf", data.getJsonObject("simple").getString("string"));
        assertEquals(30, data.getJsonObject("simple").getInt("integer"));
        assertEquals("asdfgh", data.getJsonObject("simple2").getString("string"));
        assertEquals(31, data.getJsonObject("simple2").getInt("integer"));
    }

    @Test
    public void testSimpleQueryAsync() {
        Document document = document(
                operation("Simple",
                        field("simple",
                                field("string"),
                                field("integer"))));
        JsonObject data = client.executeAsync(document)
                .await().atMost(Duration.ofSeconds(30)).getData();
        assertEquals("asdf", data.getJsonObject("simple").getString("string"));
        assertEquals(30, data.getJsonObject("simple").getInt("integer"));
    }

    @Test
    public void testSimpleQueryWithArgument() throws ExecutionException, InterruptedException {
        Document document = document(
                operation("MyAwesomeQuery",
                        field("queryWithArgument",
                                args(arg("number", 12)),
                                field("integer"))));
        Response response = client.executeSync(document);
        JsonObject data = response.getData();
        assertEquals(12, data.getJsonObject("queryWithArgument").getInt("integer"));
    }

}

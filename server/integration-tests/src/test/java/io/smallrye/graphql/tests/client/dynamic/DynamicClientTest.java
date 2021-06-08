package io.smallrye.graphql.tests.client.dynamic;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.time.Duration;
import java.util.List;
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

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClientBuilder;

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
        Document document = document(operation(
                field("simple",
                        field("string"),
                        field("integer"))));
        JsonObject data = client.executeSync(document).getData();
        assertEquals("asdf", data.getJsonObject("simple").getString("string"));
        assertEquals(30, data.getJsonObject("simple").getInt("integer"));
    }

    @Test
    public void testTwoQueriesInOneOperationSync() throws ExecutionException, InterruptedException {
        Document document = document(operation(
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
        Document document = document(operation(
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
        Document document = document(operation(
                field("queryWithArgument",
                        args(arg("number", 12)),
                        field("integer"))));
        Response response = client.executeSync(document);
        JsonObject data = response.getData();
        assertEquals(12, data.getJsonObject("queryWithArgument").getInt("integer"));
    }

    /**
     * Parsing the response into a model class that contains a renamed field (using @Name)
     */
    @Test
    public void testRenamedField() throws ExecutionException, InterruptedException {
        Document document = document(operation(
                field("withRenamedField",
                        field("renamedField:specialName"),
                        field("string"))));
        Response response = client.executeSync(document);
        Dummy ret = response.getObject(Dummy.class, "withRenamedField");
        assertEquals("foo", ret.getRenamedField());
    }

    @Test
    public void testListWithRenamedField() throws ExecutionException, InterruptedException {
        Document document = document(operation(
                field("listWithRenamedField",
                        field("renamedField:specialName"),
                        field("string"))));
        Response response = client.executeSync(document);
        List<Dummy> ret = response.getList(Dummy.class, "listWithRenamedField");
        assertEquals("foo", ret.get(0).getRenamedField());
        assertEquals("foo2", ret.get(1).getRenamedField());
    }

}

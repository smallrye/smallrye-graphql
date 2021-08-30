package io.smallrye.graphql.tests.client.dynamic;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.Variable.var;
import static io.smallrye.graphql.client.core.Variable.vars;
import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import io.smallrye.graphql.client.core.ScalarType;
import io.smallrye.graphql.client.core.Variable;
import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClient;
import io.smallrye.graphql.client.dynamic.vertx.VertxDynamicGraphQLClientBuilder;

@RunWith(Arquillian.class)
@RunAsClient
public class DynamicClientTest {

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "validation-test.war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addClasses(DynamicClientApi.class, DummyObject.class, Dummy.class);
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
    public void testQueryWithVars() throws ExecutionException, InterruptedException {
        Variable var = var("x", ScalarType.GQL_INT);
        Document document = document(operation(
                vars(var),
                field("queryWithArgument", // query name
                        args(arg("number", var)), // the query has a 'number' parameter
                        field("integer")))); // field we want to retrieve
        Map<String, Object> variableValues = Collections.singletonMap("x", 12345);
        JsonObject data = client.executeSync(document, variableValues).getData();
        assertEquals(12345, data.getJsonObject("queryWithArgument").getInt("integer"));
    }

    @Test
    public void testStringQuery() throws ExecutionException, InterruptedException {
        JsonObject data = client.executeSync("query {simple{string integer}}").getData();
        assertEquals("asdf", data.getJsonObject("simple").getString("string"));
        assertEquals(30, data.getJsonObject("simple").getInt("integer"));
    }

    @Test
    public void testStringQueryWithVars() throws ExecutionException, InterruptedException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("x", 67);
        JsonObject data = client
                .executeSync("query($x:Int) {queryWithArgument(number: $x){integer}}", vars)
                .getData();
        assertEquals(67, data.getJsonObject("queryWithArgument").getInt("integer"));
    }

    @Test
    public void testStringQueryWithObject() throws ExecutionException, InterruptedException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("x", new DummyObject("a", "b"));
        JsonObject data = client
                .executeSync("query($x: DummyObjectInput) {queryWithArgument2(obj: $x){dummyObject{a}}}", vars)
                .getData();
        System.out.println(data);
        assertEquals("a", data.getJsonObject("queryWithArgument2").getJsonObject("dummyObject").getString("a"));
    }

    @Test
    public void testStringQueryWithMultipleOperations() throws ExecutionException, InterruptedException {
        String query = "query a {simple{integer}} " +
                "query b {simple2{integer}}";
        JsonObject data = client.executeSync(query, "a").getData();
        assertEquals(30, data.getJsonObject("simple").getInt("integer"));
        data = client.executeSync(query, "b").getData();
        assertEquals(31, data.getJsonObject("simple2").getInt("integer"));
    }

    @Test
    public void testStringQueryWithName() throws ExecutionException, InterruptedException {
        JsonObject data = client.executeSync("query MyAwesomeQuery {simple{string integer}}").getData();
        assertEquals("asdf", data.getJsonObject("simple").getString("string"));
        assertEquals(30, data.getJsonObject("simple").getInt("integer"));
    }

    @Test
    public void testStringQueryUnspecified() throws ExecutionException, InterruptedException {
        JsonObject data = client.executeSync("{simple{string integer}}").getData();
        assertEquals("asdf", data.getJsonObject("simple").getString("string"));
        assertEquals(30, data.getJsonObject("simple").getInt("integer"));
    }

    @Test
    public void testStringQueryWithArguments() throws ExecutionException, InterruptedException {
        JsonObject data = client.executeSync("query {queryWithArgument(number: 20){integer}}").getData();
        assertEquals(20, data.getJsonObject("queryWithArgument").getInt("integer"));
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

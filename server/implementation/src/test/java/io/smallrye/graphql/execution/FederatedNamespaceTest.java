package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.api.federation.External;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.spi.config.Config;
import io.smallrye.graphql.test.namespace.NamedNamespaceModel;
import io.smallrye.graphql.test.namespace.NamedNamespaceTestApi;
import io.smallrye.graphql.test.namespace.NamedNamespaceWIthGroupingKeyModel;
import io.smallrye.graphql.test.namespace.NamedNamespaceWithGroupingKeyTestApi;
import io.smallrye.graphql.test.namespace.SourceNamespaceModel;
import io.smallrye.graphql.test.namespace.SourceNamespaceTestApi;
import io.smallrye.graphql.test.namespace.UnamedModel;
import io.smallrye.graphql.test.namespace.UnnamedTestApi;

/**
 * Test for Federated namespaces
 */
public class FederatedNamespaceTest {
    private static final TestConfig config = (TestConfig) Config.get();
    private static ExecutionService executionService;

    @AfterAll
    static void afterAll() {
        config.reset();
        config.federationEnabled = false;
        System.setProperty("smallrye.graphql.federation.enabled", "false");
    }

    @BeforeAll
    static void beforeAll() {
        config.federationEnabled = true;
        System.setProperty("smallrye.graphql.federation.enabled", "true");

        IndexView index = buildIndex(Directive.class, Key.class, External.class, Key.Keys.class,
                NamedNamespaceModel.class, NamedNamespaceTestApi.class,
                NamedNamespaceWIthGroupingKeyModel.class, NamedNamespaceWithGroupingKeyTestApi.class,
                SourceNamespaceModel.class, SourceNamespaceTestApi.class,
                SourceNamespaceTestApi.First.class, SourceNamespaceTestApi.Second.class,
                UnamedModel.class, UnnamedTestApi.class);

        GraphQLSchema graphQLSchema = createGraphQLSchema(index);
        Schema schema = SchemaBuilder.build(index);
        executionService = new ExecutionService(graphQLSchema, schema);
    }

    private static IndexView buildIndex(Class<?>... classes) {
        org.jboss.jandex.Indexer indexer = new org.jboss.jandex.Indexer();
        Stream.of(classes).forEach(cls -> index(indexer, cls));
        return indexer.complete();
    }

    private static InputStream getResourceStream(Class<?> type) {
        String name = type.getName().replace(".", "/") + ".class";
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }

    private static void index(org.jboss.jandex.Indexer indexer, Class<?> cls) {
        try {
            indexer.index(getResourceStream(cls));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static GraphQLSchema createGraphQLSchema(IndexView index) {
        Schema schema = SchemaBuilder.build(index);
        assertNotNull(schema, "Schema should not be null");
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema, true);
        assertNotNull(graphQLSchema, "GraphQLSchema should not be null");
        return graphQLSchema;
    }

    private static JsonObject executeAndGetResult(String graphQL) {
        JsonObjectResponseWriter jsonObjectResponseWriter = new JsonObjectResponseWriter(graphQL);
        jsonObjectResponseWriter.logInput();
        executionService.executeSync(jsonObjectResponseWriter.getInput(), jsonObjectResponseWriter);
        jsonObjectResponseWriter.logOutput();
        return jsonObjectResponseWriter.getOutput();
    }

    private void test(String type, String id) {
        JsonObject jsonObject = executeAndGetResult(TEST_QUERY.apply(type, id));
        assertNotNull(jsonObject);

        JsonValue jsonValue = jsonObject.getJsonObject("data")
                .getJsonArray("_entities")
                .getJsonObject(0)
                .get("value");
        String value = ((JsonString) jsonValue).getString();
        assertEquals(value, id);
    }

    @Test
    public void findEntityWithoutNamespace() {
        test(UnamedModel.class.getSimpleName(), "unnamed_id");
    }

    @Test
    public void findEntityWithNameNamespace() {
        test(NamedNamespaceModel.class.getSimpleName(), "named_id");
    }

    @Test
    public void findEntityWithSourceNamespace() {
        test(SourceNamespaceModel.class.getSimpleName(), "source_id");
    }

    @Test
    public void findEntityWithWithGroupedKeyAndNamespace() {
        String id = "grouped_key";

        JsonObject jsonObject = executeAndGetResult(GROUPED_KEY_QUERY.apply(
                NamedNamespaceWIthGroupingKeyModel.class.getSimpleName(),
                id));
        assertNotNull(jsonObject);

        JsonValue jsonValue = jsonObject.getJsonObject("data")
                .getJsonArray("_entities")
                .getJsonObject(0)
                .get("value");
        String value = ((JsonString) jsonValue).getString();
        assertEquals(value, id);

        jsonValue = jsonObject.getJsonObject("data")
                .getJsonArray("_entities")
                .getJsonObject(0)
                .get("anotherId");
        String anotherId = ((JsonString) jsonValue).getString();
        assertEquals(anotherId, "otherKey_" + id);
    }

    private static final BiFunction<String, String, String> GROUPED_KEY_QUERY = (type, id) -> "query {\n" +
            "_entities(\n" +
            "    representations: { id: \"" + id + "\", anotherId : \"otherKey_" + id + "\", __typename: \"" + type + "\" }\n" +
            ") {\n" +
            "    __typename\n" +
            "    ... on " + type + " {\n" +
            "        id\n" +
            "        anotherId\n" +
            "        value\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final BiFunction<String, String, String> TEST_QUERY = (type, id) -> "query {\n" +
            "_entities(\n" +
            "    representations: { id: \"" + id + "\", __typename: \"" + type + "\" }\n" +
            ") {\n" +
            "    __typename\n" +
            "    ... on " + type + " {\n" +
            "        id\n" +
            "        value\n" +
            "    }\n" +
            "  }\n" +
            "}";
}

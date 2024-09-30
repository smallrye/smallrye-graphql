package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
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
import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.External;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.api.federation.Requires;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.spi.config.Config;
import io.smallrye.graphql.test.resolver.ExtendedApi;
import io.smallrye.graphql.test.resolver.ExtendedType;

/**
 * Test for Federated namespaces
 */
public class ResolverTest {
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
                Extends.class, Requires.class, ExtendedType.class, ExtendedApi.class);

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

    @Test
    public void findByIdTest() {
        JsonObject jsonObject = executeAndGetResult(TEST_ID_QUERY);
        assertNotNull(jsonObject);

        JsonValue jsonValue = jsonObject.getJsonObject("data")
                .getJsonArray("_entities")
                .getJsonObject(0)
                .get("id");
        assertEquals(((JsonString) jsonValue).getString(), "id");

        jsonValue = jsonObject.getJsonObject("data")
                .getJsonArray("_entities")
                .getJsonObject(0)
                .get("value");
        assertNull(jsonValue);
    }

    @Test
    public void extendsTest() {
        JsonObject jsonObject = executeAndGetResult(TEST_ID_NAME_KEY_QUERY);
        assertNotNull(jsonObject);

        JsonValue jsonValue = jsonObject.getJsonObject("data")
                .getJsonArray("_entities")
                .getJsonObject(0)
                .get("id");
        assertEquals(((JsonString) jsonValue).getString(), "id");

        jsonValue = jsonObject.getJsonObject("data")
                .getJsonArray("_entities")
                .getJsonObject(0)
                .get("value");
        assertEquals(((JsonString) jsonValue).getString(), "idnamekey");
    }

    private static final String TEST_ID_QUERY = "query {\n" +
            "_entities(\n" +
            "    representations: { id: \"id\", __typename: \"ExtendedType\" }\n" +
            ") {\n" +
            "    __typename\n" +
            "    ... on ExtendedType {\n" +
            "        id\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String TEST_ID_NAME_KEY_QUERY = "query {\n" +
            "_entities(\n" +
            "    representations: { id: \"id\", name: \"name\", key: \"key\", __typename: \"ExtendedType\" }\n" +
            ") {\n" +
            "    __typename\n" +
            "    ... on ExtendedType {\n" +
            "        id\n" +
            "        name\n" +
            "        key\n" +
            "        value\n" +
            "    }\n" +
            "  }\n" +
            "}";
}

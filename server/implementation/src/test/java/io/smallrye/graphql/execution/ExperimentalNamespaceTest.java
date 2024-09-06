package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import jakarta.json.JsonObject;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.test.namespace.ExperimentalNamespaceApi;
import io.smallrye.graphql.test.namespace.ExperimentalNamespaceWithErrorApi;

/**
 * Test for Federated namespaces
 */
public class ExperimentalNamespaceTest {
    private static ExecutionService executionService;

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
    public void experimentalNamespaceTest() {
        IndexView index = buildIndex(ExperimentalNamespaceApi.class);

        GraphQLSchema graphQLSchema = createGraphQLSchema(index);
        Schema schema = SchemaBuilder.build(index);
        executionService = new ExecutionService(graphQLSchema, schema);

        JsonObject jsonObject = executeAndGetResult(NAMESPACED_QUERY);
        assertNotNull(jsonObject);

        String result = jsonObject.getJsonObject("data")
                .getJsonObject("admin")
                .getJsonObject("users")
                .getString("find");
        assertEquals(result, "AdminUsersFind");
    }

    @Test
    public void experimentalNamespaceFailureWithUsingNameTest() {
        IndexView index = buildIndex(ExperimentalNamespaceWithErrorApi.class);
        Assertions.assertThrows(RuntimeException.class, () -> SchemaBuilder.build(index));
    }

    private static final String NAMESPACED_QUERY = "query AminUsersFind {\n" +
            "  admin {\n" +
            "    users {\n" +
            "      find \n" +
            "    }\n" +
            "  }\n" +
            "}";
}

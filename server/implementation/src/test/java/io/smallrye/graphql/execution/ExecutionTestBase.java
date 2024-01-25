package io.smallrye.graphql.execution;

import java.util.Map;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Base class for execution tests
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class ExecutionTestBase {
    protected static final Logger LOG = Logger.getLogger(ExecutionTestBase.class.getName());

    protected ExecutionService executionService;

    @BeforeEach
    public void init() {
        IndexView index = getIndex();
        Schema schema = SchemaBuilder.build(index);
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema);

        SchemaPrinter printer = new SchemaPrinter();
        String schemaString = printer.print(graphQLSchema);
        LOG.info("================== Testing against: ====================");
        LOG.info(schemaString);
        LOG.info("========================================================");
        this.executionService = new ExecutionService(graphQLSchema, schema);
    }

    protected IndexView getIndex() {
        return Indexer.getAllTestIndex();
    }

    protected JsonObject executeAndGetData(String graphQL) {
        return executeAndGetData(graphQL, null);
    }

    protected JsonObject executeAndGetData(String graphQL, Map<String, Object> context) {
        JsonObject result = executeAndGetResult(graphQL, context);
        JsonValue value = result.get(DATA);
        if (value != null) {
            return result.getJsonObject(DATA);
        }
        return JsonObject.EMPTY_JSON_OBJECT;
    }

    protected JsonArray executeAndGetErrors(String graphQL) {
        return executeAndGetErrors(graphQL, null);
    }

    protected JsonArray executeAndGetErrors(String graphQL, Map<String, Object> context) {
        JsonObject result = executeAndGetResult(graphQL, context);
        JsonValue value = result.get(ERRORS);
        if (value != null) {
            return result.getJsonArray(ERRORS);
        }
        return null;
    }

    protected JsonObject executeAndGetExtensions(String graphQL) {
        return executeAndGetExtensions(graphQL, null);
    }

    protected JsonObject executeAndGetExtensions(String graphQL, Map<String, Object> context) {
        JsonObject result = executeAndGetResult(graphQL, context);
        JsonValue value = result.get(EXTENSIONS);
        if (value != null) {
            return result.getJsonObject(EXTENSIONS);
        }
        return null;
    }

    protected JsonObject executeAndGetResult(String graphQL, Map<String, Object> context) {
        JsonObjectResponseWriter jsonObjectResponseWriter = new JsonObjectResponseWriter(graphQL);
        jsonObjectResponseWriter.logInput();
        if (context == null) {
            executionService.executeSync(jsonObjectResponseWriter.getInput(), jsonObjectResponseWriter);
        } else {
            executionService.executeSync(jsonObjectResponseWriter.getInput(), context, jsonObjectResponseWriter);
        }
        jsonObjectResponseWriter.logOutput();

        return jsonObjectResponseWriter.getOutput();
    }

    private static final String DATA = "data";
    private static final String ERRORS = "errors";
    private static final String EXTENSIONS = "extensions";

}

package io.smallrye.graphql.execution;

import java.util.Map;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

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

    protected ObjectNode executeAndGetData(String graphQL) {
        return executeAndGetData(graphQL, null);
    }

    protected ObjectNode executeAndGetData(String graphQL, Map<String, Object> context) {
        ObjectNode result = executeAndGetResult(graphQL, context);
        JsonNode value = result.get(DATA);
        if (value != null) {
            return (ObjectNode) result.get(DATA);
        }
        return JsonNodeFactory.instance.objectNode();
    }

    protected ArrayNode executeAndGetErrors(String graphQL) {
        return executeAndGetErrors(graphQL, null);
    }

    protected ArrayNode executeAndGetErrors(String graphQL, Map<String, Object> context) {
        ObjectNode result = executeAndGetResult(graphQL, context);
        JsonNode value = result.get(ERRORS);
        if (value != null) {
            return (ArrayNode) result.get(ERRORS);
        }
        return null;
    }

    protected ObjectNode executeAndGetExtensions(String graphQL) {
        return executeAndGetExtensions(graphQL, null);
    }

    protected ObjectNode executeAndGetExtensions(String graphQL, Map<String, Object> context) {
        ObjectNode result = executeAndGetResult(graphQL, context);
        JsonNode value = result.get(EXTENSIONS);
        if (value != null) {
            return (ObjectNode) result.get(EXTENSIONS);
        }
        return null;
    }

    protected ObjectNode executeAndGetResult(String graphQL, Map<String, Object> context) {
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

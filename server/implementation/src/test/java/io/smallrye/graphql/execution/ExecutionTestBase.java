package io.smallrye.graphql.execution;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.bootstrap.Config;
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

        SchemaPrinter printer = new SchemaPrinter(getGraphQLConfig());
        String schemaString = printer.print(graphQLSchema);
        LOG.info("================== Testing against: ====================");
        LOG.info(schemaString);
        LOG.info("========================================================");
        this.executionService = new ExecutionService(getGraphQLConfig(), graphQLSchema, schema.getBatchOperations(), true);
    }

    protected IndexView getIndex() {
        return Indexer.getAllTestIndex();
    }

    protected JsonObject executeAndGetData(String graphQL) {
        JsonObject result = executeAndGetResult(graphQL);
        JsonValue value = result.get(DATA);
        if (value != null) {
            return result.getJsonObject(DATA);
        }
        return null;
    }

    protected JsonArray executeAndGetErrors(String graphQL) {
        JsonObject result = executeAndGetResult(graphQL);
        JsonValue value = result.get(ERRORS);
        if (value != null) {
            return result.getJsonArray(ERRORS);
        }
        return null;
    }

    protected JsonObject executeAndGetResult(String graphQL) {

        JsonObject input = toJsonObject(graphQL);
        String prettyInput = getPrettyJson(input);
        LOG.info(prettyInput);
        ExecutionResponse result = executionService.execute(input);

        String prettyData = getPrettyJson(result.getExecutionResultAsJsonObject());
        LOG.info(prettyData);

        return result.getExecutionResultAsJsonObject();
    }

    private JsonObject toJsonObject(String graphQL) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("query", graphQL);
        return builder.build();
    }

    private Config getGraphQLConfig() {
        Config config = new Config() {

            @Override
            public boolean isPrintDataFetcherException() {
                return true;
            }

            @Override
            public <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
                if (key.equals(TestEventingService.KEY)) {
                    return (T) new Boolean(true);
                }
                return Config.super.getConfigValue(key, type, defaultValue);
            }

            @Override
            public Optional<List<String>> getErrorExtensionFields() {
                return Optional.of(Config.ERROR_EXTENSION_ALL_KNOWN);
            }
        };
        return config;
    }

    private String getPrettyJson(JsonObject jsonObject) {

        JsonWriterFactory writerFactory = Json.createWriterFactory(JSON_PROPERTIES);

        try (StringWriter sw = new StringWriter();
                JsonWriter jsonWriter = writerFactory.createWriter(sw)) {
            jsonWriter.writeObject(jsonObject);
            return sw.toString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    private static final String DATA = "data";
    private static final String ERRORS = "errors";

    private static final Map<String, Object> JSON_PROPERTIES = new HashMap<>(1);

    static {
        JSON_PROPERTIES.put(JsonGenerator.PRETTY_PRINTING, true);
    }

}

package io.smallrye.graphql.execution;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;

import org.dataloader.DataLoaderRegistry;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.BeforeEach;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.bootstrap.BootstrapedResult;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.schema.model.TypeAutoNameStrategy;

/**
 * Base class for execution tests
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionTestBase {
    protected static final Logger LOG = Logger.getLogger(ExecutionTestBase.class.getName());

    protected ExecutionService executionService;

    @BeforeEach
    public void init() {
        IndexView index = Indexer.getTCKIndex();
        Schema schema = SchemaBuilder.build(index, TypeAutoNameStrategy.Default);
        BootstrapedResult bootstraped = Bootstrap.bootstrap(schema);
        GraphQLSchema graphQLSchema = bootstraped.getGraphQLSchema();
        DataLoaderRegistry dataLoaderRegistry = bootstraped.getDataLoaderRegistry();
        this.executionService = new ExecutionService(getGraphQLConfig(), graphQLSchema, dataLoaderRegistry);
    }

    protected JsonObject executeAndGetData(String graphQL) {
        JsonObject result = executionService.execute(toJsonObject(graphQL));

        String prettyData = getPrettyJson(result);
        LOG.info(prettyData);

        return result.getJsonObject(DATA);
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

    private static final Map<String, Object> JSON_PROPERTIES = new HashMap<>(1);

    static {
        JSON_PROPERTIES.put(JsonGenerator.PRETTY_PRINTING, true);
    }

}

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

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Test a basic query
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionTest {
    private static final Logger LOG = Logger.getLogger(ExecutionTest.class.getName());

    private ExecutionService executionService;

    @Before
    public void init() {
        IndexView index = Indexer.getTCKIndex();
        Schema schema = SchemaBuilder.build(index);
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema);

        this.executionService = new ExecutionService(getGraphQLConfig(), graphQLSchema);
    }

    @Test
    public void testBasicQuery() throws IOException {
        JsonObject data = executeAndGetData(TEST_QUERY);

        JsonObject testObject = data.getJsonObject("testObject");

        Assert.assertNotNull(testObject);

        Assert.assertFalse("name should not be null", testObject.isNull("name"));
        Assert.assertEquals("Phillip", testObject.getString("name"));

        Assert.assertFalse("id should not be null", testObject.isNull("id"));

    }

    private JsonObject executeAndGetData(String graphQL) {
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

    private static final String TEST_QUERY = "{\n" +
            "  testObject(yourname:\"Phillip\") {\n" +
            "    id\n" +
            "    name\n" +
            "  }\n" +
            "}";

    private static Map<String, Object> JSON_PROPERTIES = new HashMap<>(1);

    static {
        JSON_PROPERTIES.put(JsonGenerator.PRETTY_PRINTING, true);
    }

}

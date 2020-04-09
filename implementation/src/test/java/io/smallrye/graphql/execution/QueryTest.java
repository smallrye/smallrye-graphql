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
import org.jboss.weld.junit4.WeldInitiator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.Indexer;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Test a basic query
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class QueryTest {
    private static final Logger LOG = Logger.getLogger(QueryTest.class.getName());

    private ExecutionService executionService;

    @Rule
    public WeldInitiator weld = WeldInitiator.of(heroFinder, heroDatabase, sidekickDatabase, heroLocator);

    @Before
    public void init() {
        IndexView index = Indexer.getTCKIndex();
        Schema schema = SchemaBuilder.build(index);
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema);

        this.executionService = new ExecutionService(getGraphQLConfig(), graphQLSchema);
        executionService.init();

    }

    @Test
    public void testQuery() throws IOException {
        JsonObject result = executionService.execute(toJsonObject(GET_HERO));

        String prettyData = getPrettyJson(result);
        LOG.info(prettyData);

        JsonObject data = result.getJsonObject(DATA);

        JsonObject superHero = data.getJsonObject("superHero");

        Assert.assertEquals("Iron Man", superHero.getString("name"));
        Assert.assertEquals("Los Angeles, CA", superHero.getString("primaryLocation"));
        Assert.assertEquals("Tony Stark", superHero.getString("realName"));
    }

    private JsonObject toJsonObject(String graphQL) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("query", graphQL);
        return builder.build();
    }

    private GraphQLConfig getGraphQLConfig() {
        GraphQLConfig config = new GraphQLConfig();
        config.setAllowGet(false);
        config.setPrintDataFetcherException(true);
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

    private static final String GET_HERO = "{\n" +
            "  superHero(name:\"Iron Man\") {\n" +
            "    \n" +
            "    name\n" +
            "    primaryLocation\n" +
            "    realName\n" +
            "    superPowers\n" +
            "    teamAffiliations {\n" +
            "      name\n" +
            "      members{\n" +
            "        name\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    
    // Create the CDI Beans in the TCK Tests app
    private static Class heroFinder;
    private static Class heroDatabase;
    private static Class sidekickDatabase;
    private static Class heroLocator;

    private static Map<String, Object> JSON_PROPERTIES = new HashMap<>(1);

    static {
        try {
            heroFinder = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.api.HeroFinder");
            heroDatabase = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.db.HeroDatabase");
            sidekickDatabase = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.db.SidekickDatabase");
            heroLocator = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.db.HeroLocator");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        JSON_PROPERTIES.put(JsonGenerator.PRETTY_PRINTING, true);
    }

}

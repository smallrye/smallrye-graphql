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
    public WeldInitiator weld = WeldInitiator.of(heroFinder, heroDatabase, sidekickDatabase, heroLocator, scalarTestApi);

    @Before
    public void init() {
        IndexView index = Indexer.getTCKIndex();
        Schema schema = SchemaBuilder.build(index);
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema);

        this.executionService = new ExecutionService(getGraphQLConfig(), graphQLSchema);
        executionService.init();

    }

    @Test
    public void testBasicQuery() throws IOException {
        JsonObject result = executionService.execute(toJsonObject(GET_HERO));

        String prettyData = getPrettyJson(result);
        LOG.info(prettyData);

        JsonObject data = result.getJsonObject(DATA);

        JsonObject superHero = data.getJsonObject("superHero");

        Assert.assertEquals("Iron Man", superHero.getString("name"));
        Assert.assertEquals("Los Angeles, CA", superHero.getString("primaryLocation"));
        Assert.assertEquals("Tony Stark", superHero.getString("realName"));
    }

    @Test
    public void testTransformedDateOnQuery() throws IOException {
        JsonObject result = executionService.execute(toJsonObject(TRANSFORMED_DATE));

        String prettyData = getPrettyJson(result);
        LOG.info(prettyData);

        JsonObject data = result.getJsonObject(DATA);

        String transformedDate = data.getString("transformedDate");

        Assert.assertEquals("16 Aug 2016", transformedDate);

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

    private static final String TRANSFORMED_DATE = "query testTransformedDate{\n" +
            "  	transformedDate\n" +
            "}";

    private static final String GET_HERO = "{\n" +
            "  superHero(name:\"Iron Man\") {\n" +
            "    idNumber\n" + // To test number formatting
            "    name\n" +
            "    primaryLocation\n" +
            "    realName\n" +
            "    superPowers\n" +
            "    dateOfLastCheckin\n" + // To test date formatting
            "    timeOfLastBattle\n" + // To test dateTime formatting
            "    patrolStartTime\n" + // To test time formatting
            "    bankBalance\n" + // To test number formatting
            //"    currentLocation\n" + // To test Source
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

    private static Class scalarTestApi;

    private static Map<String, Object> JSON_PROPERTIES = new HashMap<>(1);

    static {
        try {
            heroFinder = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.api.HeroFinder");
            heroDatabase = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.db.HeroDatabase");
            sidekickDatabase = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.db.SidekickDatabase");
            heroLocator = Class.forName("org.eclipse.microprofile.graphql.tck.apps.superhero.db.HeroLocator");
            scalarTestApi = Class.forName("org.eclipse.microprofile.graphql.tck.apps.basic.api.ScalarTestApi");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        JSON_PROPERTIES.put(JsonGenerator.PRETTY_PRINTING, true);
    }

}

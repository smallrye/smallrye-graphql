package io.smallrye.graphql.execution;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
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
public class ExecutionTest {
    private static final Logger LOG = Logger.getLogger(ExecutionTest.class.getName());

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
        JsonObject data = executeAndGetData(GET_HERO);

        JsonObject superHero = data.getJsonObject("superHero");

        Assert.assertNotNull(superHero);

        Assert.assertFalse("name should not be null", superHero.isNull("name"));
        Assert.assertEquals("Iron Man", superHero.getString("name"));

        Assert.assertFalse("primaryLocation should not be null", superHero.isNull("primaryLocation"));
        Assert.assertEquals("Los Angeles, CA", superHero.getString("primaryLocation"));

        Assert.assertFalse("realName should not be null", superHero.isNull("realName"));
        Assert.assertEquals("Tony Stark", superHero.getString("realName"));

        // To test @Source
        Assert.assertFalse("currentLocation should not be null (@Source not working)", superHero.isNull("currentLocation"));
        Assert.assertEquals("Wachovia", superHero.getString("currentLocation"));

        // To test @Source with extra default parameter
        Assert.assertFalse("secretToken should not be null (@Source with extra parameter not working)",
                superHero.isNull("secretToken"));
        Assert.assertTrue(
                superHero.getJsonObject("secretToken").getString("value").startsWith("********-****-****-****-********"));

        // To test Number formatting (on return object fields)
        Assert.assertFalse("idNumber should not be null", superHero.isNull("idNumber"));
        Assert.assertEquals("ID-12345678", superHero.getString("idNumber"));

        // To test Date formatting (on return object fields)
        Assert.assertFalse("Transformation on Date not working", superHero.isNull("dateOfLastCheckin"));
        Assert.assertEquals("09/09/2019", superHero.getString("dateOfLastCheckin"));

        // To test DateTime formatting (on return object fields)
        Assert.assertFalse("Transformation on DateTime not working", superHero.isNull("timeOfLastBattle"));
        Assert.assertEquals("08:30:01 06-09-2019", superHero.getString("timeOfLastBattle"));

        // To test Time formatting (on return object fields)
        Assert.assertFalse("Transformation on Time not working", superHero.isNull("patrolStartTime"));
        Assert.assertEquals("08:00", superHero.getString("patrolStartTime"));

    }

    @Test
    public void testDateTransformationOnQuery() throws IOException {
        JsonObject data = executeAndGetData(TRANSFORMED_DATE);

        Assert.assertFalse("transformedDate should not be null", data.isNull("transformedDate"));
        String transformedDate = data.getString("transformedDate");

        Assert.assertEquals("Date transformation on Query not working", "16 Aug 2016", transformedDate);

    }

    @Test
    public void testNumberTransformationOnMutation() throws IOException {
        JsonObject data = executeAndGetData(TRANSFORMED_NUMBER);

        Assert.assertFalse("transformedNumber should not be null", data.isNull("transformedNumber"));
        Assert.assertEquals("Number transformation on Mutation not working", "number 345", data.getString("transformedNumber"));
    }

    @Test
    public void testNumberTransformationOnArgument() throws IOException {
        JsonObject data = executeAndGetData(TRANSFORMED_ARGUMENT);

        Assert.assertFalse("idNumberWithCorrectFormat should not be null", data.isNull("idNumberWithCorrectFormat"));
        Assert.assertFalse("idNumber should not be null", data.getJsonObject("idNumberWithCorrectFormat").isNull("idNumber"));
        Assert.assertEquals("Number transformation on Argument not working", "ID-88888888",
                data.getJsonObject("idNumberWithCorrectFormat").getString("idNumber"));

    }

    @Test
    public void testBasicMutation() throws IOException {
        JsonObject data = executeAndGetData(MUTATION_BASIC);

        Assert.assertFalse("addHeroToTeam should not be null", data.isNull("addHeroToTeam"));

        Assert.assertFalse("name should not be null", data.getJsonObject("addHeroToTeam").isNull("name"));
        Assert.assertEquals("Wrong team name while adding member", "Avengers",
                data.getJsonObject("addHeroToTeam").getString("name"));

        Assert.assertFalse("members should not be null", data.getJsonObject("addHeroToTeam").isNull("members"));
        Assert.assertEquals("Wrong team size while adding member", 4,
                data.getJsonObject("addHeroToTeam").getJsonArray("members").size());

    }

    @Test
    public void testMutationWithObjectArgument() throws IOException {
        JsonObject data = executeAndGetData(MUTATION_COMPLEX);

        Assert.assertFalse("createNewHero should not be null", data.isNull("createNewHero"));

        Assert.assertFalse("name should not be null", data.getJsonObject("createNewHero").isNull("name"));
        Assert.assertEquals("Wrong name while adding hero", "Captain America",
                data.getJsonObject("createNewHero").getString("name"));

        Assert.assertFalse("superPowers should not be null", data.getJsonObject("createNewHero").isNull("superPowers"));
        Assert.assertEquals("Wrong size superPowers while adding member", 2,
                data.getJsonObject("createNewHero").getJsonArray("superPowers").size());

    }

    @Test
    public void testMutationScalarJavaMapping() throws IOException {
        JsonObject data = executeAndGetData(MUTATION_SCALAR_MAPPING);

        Assert.assertFalse("provisionHero should not be null", data.isNull("provisionHero"));

        Assert.assertFalse("name should not be null", data.getJsonObject("provisionHero").isNull("name"));
        Assert.assertEquals("Wrong name while provisioning hero", "Starlord",
                data.getJsonObject("provisionHero").getString("name"));

        Assert.assertFalse("equipment should not be null", data.getJsonObject("provisionHero").isNull("equipment"));
        Assert.assertEquals("Wrong size equipment while provisioning member", 1,
                data.getJsonObject("provisionHero").getJsonArray("equipment").size());

    }

    @Test
    public void testMutationWithComplexDefault() throws IOException {
        JsonObject data = executeAndGetData(MUTATION_COMPLEX_DEFAULT);

        Assert.assertFalse("provisionHero should not be null", data.isNull("provisionHero"));

        Assert.assertFalse("name should not be null", data.getJsonObject("provisionHero").isNull("name"));
        Assert.assertEquals("Wrong name while provisioning hero", "Spider Man",
                data.getJsonObject("provisionHero").getString("name"));

        Assert.assertFalse("equipment should not be null", data.getJsonObject("provisionHero").isNull("equipment"));
        Assert.assertEquals("Wrong size equipment while provisioning member", 1,
                data.getJsonObject("provisionHero").getJsonArray("equipment").size());

    }

    @Test
    public void testMutationWithArrayInput() throws IOException {
        JsonObject data = executeAndGetData(MUTATION_COMPLEX_ARRAY);

        Assert.assertFalse("createNewHeroesWithArray should not be null", data.isNull("createNewHeroesWithArray"));

        Assert.assertEquals("Wrong size array while createNewHeroesWithArray", 1,
                data.getJsonArray("createNewHeroesWithArray").size());

    }

    @Test
    public void testMutationWithCollectionInput() throws IOException {
        JsonObject data = executeAndGetData(MUTATION_COMPLEX_COLLECTION);

        Assert.assertFalse("createNewHeroes should not be null", data.isNull("createNewHeroes"));

        Assert.assertEquals("Wrong size array while createNewHeroes", 1,
                data.getJsonArray("createNewHeroes").size());

    }

    @Test
    public void testMutationWithCollectionTransformationInput() throws IOException {
        JsonObject data = executeAndGetData(MUTATION_COMPLEX_TRANSFORMATION_COLLECTION);

        Assert.assertFalse("createNewHero should not be null", data.isNull("createNewHero"));

        Assert.assertFalse("name should not be null", data.getJsonObject("createNewHero").isNull("name"));
        Assert.assertEquals("Wrong name while createNewHero hero", "Black Panther",
                data.getJsonObject("createNewHero").getString("name"));

        Assert.assertFalse("agesOfKids should not be null", data.getJsonObject("createNewHero").isNull("agesOfKids"));

        JsonArray jsonArray = data.getJsonObject("createNewHero").getJsonArray("agesOfKids");
        Assert.assertEquals("Wrong size agesOfKids while createNewHero member", 2,
                jsonArray.size());

        Object[] receivedKids = new Object[] { jsonArray.getJsonString(0).toString(), jsonArray.getJsonString(1).toString() };
        Object[] expectedKids = new Object[] { "\"3 years\"", "\"5 years\"" };

        Assert.assertArrayEquals(expectedKids, receivedKids);

    }

    @Test
    public void testMutationWithScalarDateInput() throws IOException {
        JsonObject data = executeAndGetData(MUTATION_SCALAR_DATE_INPUT);

        Assert.assertFalse("startPatrolling should not be null", data.isNull("startPatrolling"));

        Assert.assertFalse("name should not be null", data.getJsonObject("startPatrolling").isNull("name"));
        Assert.assertEquals("Wrong name while startPatrolling", "Starlord",
                data.getJsonObject("startPatrolling").getString("name"));

        Assert.assertFalse("patrolStartTime should not be null",
                data.getJsonObject("startPatrolling").isNull("patrolStartTime"));
        Assert.assertEquals("Wrong time while patrolStartTime", "20:00",
                data.getJsonObject("startPatrolling").getString("patrolStartTime"));
    }

    @Test
    public void testMutationWithScalarNumberInput() throws IOException {
        JsonObject data = executeAndGetData(MUTATION_SCALAR_NUMBER_INPUT);

        Assert.assertFalse("idNumber should not be null", data.isNull("idNumber"));
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

    private static final String MUTATION_SCALAR_NUMBER_INPUT = "mutation setHeroIdNumber {\n" +
            "  idNumber(name:\"Starlord\", id:77777777) {\n" +
            "    name\n" +
            "    idNumber\n" +
            "  }\n" +
            "}";

    // This test Scalars as input to operations
    private static final String MUTATION_SCALAR_DATE_INPUT = "mutation heroStartPatrolling {\n" +
            "  startPatrolling(name:\"Starlord\", time:\"20:00:00\") {\n" +
            "    name\n" +
            "    patrolStartTime\n" +
            "  }\n" +
            "}";

    // This test transformation in collections as input arguments
    private static final String MUTATION_COMPLEX_TRANSFORMATION_COLLECTION = "mutation createNewHero {\n" +
            "    createNewHero(hero:{\n" +
            "            name: \"Black Panther\"\n" +
            "            realName: \"T'Challa\"\n" +
            "            superPowers: [\"Top intellect\"]\n" +
            "            primaryLocation: \"Wakanda, Africa\"\n" +
            "            teamAffiliations: [{name: \"Avengers\"}]\n" +
            "            idNumber: \"ID-98701234\",\n" +
            "            kidsAges: [\"3 years old\",\"5 years old\"]\n" +
            "        }) {\n" +
            "            name\n" +
            "            primaryLocation\n" +
            "            superPowers\n" +
            "            realName\n" +
            "            idNumber\n" +
            "            agesOfKids\n" +
            "        }\n" +
            "}";

    // This test collections as input arguments
    private static final String MUTATION_COMPLEX_COLLECTION = "mutation createNewHeroes {\n" +
            "    createNewHeroes(heroes:[{\n" +
            "            name: \"Captain America\"\n" +
            "            realName: \"Steven Rogers\"\n" +
            "            superPowers: [\"Super strength\", \"Vibranium Shield\"]\n" +
            "            primaryLocation: \"New York, NY\"\n" +
            "            teamAffiliations: [{name: \"Avengers\"}]\n" +
            "            idNumber: \"ID-65784312\"\n" +
            "        }]) {\n" +
            "            name\n" +
            "            primaryLocation\n" +
            "            superPowers\n" +
            "            realName\n" +
            "            idNumber\n" +
            "        }\n" +
            "}";

    // This test arrays as input arguments
    private static final String MUTATION_COMPLEX_ARRAY = "mutation createNewHeroesWithArray {\n" +
            "    createNewHeroesWithArray(heroes:[{\n" +
            "            name: \"Captain America\"\n" +
            "            realName: \"Steven Rogers\"\n" +
            "            superPowers: [\"Super strength\", \"Vibranium Shield\"]\n" +
            "            primaryLocation: \"New York, NY\"\n" +
            "            teamAffiliations: [{name: \"Avengers\"}]\n" +
            "            idNumber: \"ID-65784312\"\n" +
            "        }]) {\n" +
            "            name\n" +
            "            primaryLocation\n" +
            "            superPowers\n" +
            "            realName\n" +
            "            idNumber\n" +
            "        }\n" +
            "}";

    // This tests complex default values as input
    private static final String MUTATION_COMPLEX_DEFAULT = "mutation giveSpiderManACape {\n" +
            "  provisionHero(hero:\"Spider Man\") {\n" +
            "    name,\n" +
            "    equipment {\n" +
            "      id,\n" +
            "      name,\n" +
            "      height,\n" +
            "      weight,\n" +
            "      powerLevel,\n" +
            "      supernatural\n" +
            "    }\n" +
            "  }\n" +
            "}";

    // This tests scalar-to-Java and vice-versa mappings
    private static final String MUTATION_SCALAR_MAPPING = "mutation provisionInfinityStone {\n" +
            "  provisionHero(hero:\"Starlord\", item:{\n" +
            "    id:\"12345\",\n" +
            "    name:\"Power Infinity Stone\",\n" +
            "    height:0.001,\n" +
            "    weight:0.2,\n" +
            "    powerLevel:99,\n" +
            "    supernatural:true\n" +
            "  }) {\n" +
            "    name,\n" +
            "    equipment {\n" +
            "      id,\n" +
            "      name,\n" +
            "      height,\n" +
            "      weight,\n" +
            "      powerLevel,\n" +
            "      supernatural\n" +
            "    }\n" +
            "  }\n" +
            "}";

    // This test a complex mutation (object input argument)
    private static final String MUTATION_COMPLEX = "mutation createNewHero {\n" +
            "    createNewHero(hero:{\n" +
            "            name: \"Captain America\"\n" +
            "            realName: \"Steven Rogers\"\n" +
            "            superPowers: [\"Super strength\", \"Vibranium Shield\"]\n" +
            "            primaryLocation: \"New York, NY\"\n" +
            "            teamAffiliations: [{name: \"Avengers\"}]\n" +
            "            idNumber: \"ID-65784312\"\n" +
            "        }) {\n" +
            "            name\n" +
            "            primaryLocation\n" +
            "            superPowers\n" +
            "            realName\n" +
            "            idNumber\n" +
            "        }\n" +
            "}";

    // This test a basic mutation        
    private static final String MUTATION_BASIC = "mutation addHeroToTeam {\n" +
            "    addHeroToTeam(hero: \"Starlord\", team: \"Avengers\") {\n" +
            "        name\n" +
            "        members {\n" +
            "            name\n" +
            "        }\n" +
            "    }\n" +
            "}";

    // This test date transformation on a Mutation
    private static final String TRANSFORMED_NUMBER = "mutation testTransformedNumber{\n" +
            "    transformedNumber(input:345)\n" +
            "}";

    // This test date transformation on a Query 
    private static final String TRANSFORMED_DATE = "query testTransformedDate{\n" +
            "  	transformedDate\n" +
            "}";

    // This test transformation on an argument
    private static final String TRANSFORMED_ARGUMENT = "mutation setHeroIdNumber {\n" +
            "  idNumberWithCorrectFormat(name:\"Starlord\", id:\"ID-88888888\") {\n" +
            "    name\n" +
            "    idNumber\n" +
            "  }\n" +
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
            "    currentLocation\n" + // To test Source
            "    secretToken{\n" + // To test Source with extra parameter
            "        value\n" +
            "    }" +
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

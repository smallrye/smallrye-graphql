package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import org.dataloader.DataLoaderRegistry;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldJunit5Extension;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.bootstrap.BootstrapedResult;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.cdi.CdiLookupService;
import io.smallrye.graphql.cdi.event.EventsService;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Test a basic query
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ExtendWith(WeldJunit5Extension.class)
public class CdiExecutionTest {
    private static final Logger LOG = Logger.getLogger(CdiExecutionTest.class.getName());

    private ExecutionService executionService;

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.of(heroFinder, heroDatabase, sidekickDatabase, heroLocator, scalarTestApi,
            EventsService.class, CdiLookupService.class);

    @BeforeEach
    public void init() {
        IndexView index = Indexer.getTCKIndex();
        Schema schema = SchemaBuilder.build(index);
        BootstrapedResult bootstraped = Bootstrap.bootstrap(schema);
        GraphQLSchema graphQLSchema = bootstraped.getGraphQLSchema();
        DataLoaderRegistry dataLoaderRegistry = bootstraped.getDataLoaderRegistry();
        this.executionService = new ExecutionService(getGraphQLConfig(), graphQLSchema, dataLoaderRegistry);
    }

    @Test
    public void testBasicQuery() {
        JsonObject data = executeAndGetData(GET_HERO);

        JsonObject superHero = data.getJsonObject("superHero");

        assertNotNull(superHero);

        assertFalse(superHero.isNull("name"), "name should not be null");
        assertEquals("Iron Man", superHero.getString("name"));

        assertFalse(superHero.isNull("primaryLocation"), "primaryLocation should not be null");
        assertEquals("Los Angeles, CA", superHero.getString("primaryLocation"));

        assertFalse(superHero.isNull("realName"), "realName should not be null");
        assertEquals("Tony Stark", superHero.getString("realName"));

        // To test @Source
        assertFalse(superHero.isNull("currentLocation"), "currentLocation should not be null (@Source not working)");
        assertEquals("Wachovia", superHero.getString("currentLocation"));

        // To test @Source with extra default parameter
        assertFalse(superHero.isNull("secretToken"),
                "secretToken should not be null (@Source with extra parameter not working)");
        assertTrue(
                superHero.getJsonObject("secretToken").getString("value").startsWith("********-****-****-****-********"));

        // To test Number formatting (on return object fields)
        assertFalse(superHero.isNull("idNumber"), "idNumber should not be null");
        assertEquals("ID-12345678", superHero.getString("idNumber"));

        // To test Date formatting (on return object fields)
        assertFalse(superHero.isNull("dateOfLastCheckin"), "Transformation on Date not working");
        assertEquals("09/09/2019", superHero.getString("dateOfLastCheckin"));

        // To test DateTime formatting (on return object fields)
        assertFalse(superHero.isNull("timeOfLastBattle"), "Transformation on DateTime not working");
        assertEquals("08:30:01 06-09-2019", superHero.getString("timeOfLastBattle"));

        // To test Time formatting (on return object fields)
        assertFalse(superHero.isNull("patrolStartTime"), "Transformation on Time not working");
        assertEquals("08:00", superHero.getString("patrolStartTime"));

    }

    @Test
    public void testDateTransformationOnQuery() {
        JsonObject data = executeAndGetData(TRANSFORMED_DATE);

        assertFalse(data.isNull("transformedDate"), "transformedDate should not be null");
        String transformedDate = data.getString("transformedDate");

        assertEquals("16 Aug 2016", transformedDate, "Date transformation on Query not working");
    }

    @Test
    public void testNumberTransformationOnMutation() {
        JsonObject data = executeAndGetData(TRANSFORMED_NUMBER);

        assertFalse(data.isNull("transformedNumber"), "transformedNumber should not be null");
        assertEquals("number 345", data.getString("transformedNumber"), "Number transformation on Mutation not working");
    }

    @Test
    public void testNumberTransformationOnArgument() {
        JsonObject data = executeAndGetData(TRANSFORMED_ARGUMENT);

        assertFalse(data.isNull("idNumberWithCorrectFormat"), "idNumberWithCorrectFormat should not be null");
        assertFalse(data.getJsonObject("idNumberWithCorrectFormat").isNull("idNumber"), "idNumber should not be null");
        assertEquals("ID-88888888", data.getJsonObject("idNumberWithCorrectFormat").getString("idNumber"),
                "Number transformation on Argument not working");

    }

    @Test
    public void testBasicMutation() {
        JsonObject data = executeAndGetData(MUTATION_BASIC);

        assertFalse(data.isNull("addHeroToTeam"), "addHeroToTeam should not be null");

        assertFalse(data.getJsonObject("addHeroToTeam").isNull("name"), "name should not be null");
        assertEquals("Avengers", data.getJsonObject("addHeroToTeam").getString("name"),
                "Wrong team name while adding member");

        assertFalse(data.getJsonObject("addHeroToTeam").isNull("members"), "members should not be null");
        assertEquals(4, data.getJsonObject("addHeroToTeam").getJsonArray("members").size(),
                "Wrong team size while adding member");

    }

    @Test
    public void testMutationWithObjectArgument() {
        JsonObject data = executeAndGetData(MUTATION_COMPLEX);

        assertFalse(data.isNull("createNewHero"), "createNewHero should not be null");

        assertFalse(data.getJsonObject("createNewHero").isNull("name"), "name should not be null");
        assertEquals("Captain America", data.getJsonObject("createNewHero").getString("name"),
                "Wrong name while adding hero");

        assertFalse(data.getJsonObject("createNewHero").isNull("superPowers"), "superPowers should not be null");
        assertEquals(2, data.getJsonObject("createNewHero").getJsonArray("superPowers").size(),
                "Wrong size superPowers while adding member");

    }

    @Test
    public void testMutationScalarJavaMapping() {
        JsonObject data = executeAndGetData(MUTATION_SCALAR_MAPPING);

        assertFalse(data.isNull("provisionHero"), "provisionHero should not be null");

        assertFalse(data.getJsonObject("provisionHero").isNull("name"), "name should not be null");
        assertEquals("Starlord", data.getJsonObject("provisionHero").getString("name"),
                "Wrong name while provisioning hero");

        assertFalse(data.getJsonObject("provisionHero").isNull("equipment"), "equipment should not be null");
        assertEquals(1, data.getJsonObject("provisionHero").getJsonArray("equipment").size(),
                "Wrong size equipment while provisioning member");

    }

    @Test
    public void testMutationWithComplexDefault() {
        JsonObject data = executeAndGetData(MUTATION_COMPLEX_DEFAULT);

        assertFalse(data.isNull("provisionHero"), "provisionHero should not be null");

        assertFalse(data.getJsonObject("provisionHero").isNull("name"), "name should not be null");
        assertEquals("Spider Man", data.getJsonObject("provisionHero").getString("name"),
                "Wrong name while provisioning hero");

        assertFalse(data.getJsonObject("provisionHero").isNull("equipment"), "equipment should not be null");
        assertEquals(1, data.getJsonObject("provisionHero").getJsonArray("equipment").size(),
                "Wrong size equipment while provisioning member");

    }

    @Test
    public void testMutationWithArrayInput() {
        JsonObject data = executeAndGetData(MUTATION_COMPLEX_ARRAY);

        assertFalse(data.isNull("createNewHeroesWithArray"), "createNewHeroesWithArray should not be null");

        assertEquals(1, data.getJsonArray("createNewHeroesWithArray").size(),
                "Wrong size array while createNewHeroesWithArray");

    }

    @Test
    public void testMutationWithCollectionInput() {
        JsonObject data = executeAndGetData(MUTATION_COMPLEX_COLLECTION);

        assertFalse(data.isNull("createNewHeroes"), "createNewHeroes should not be null");

        assertEquals(1, data.getJsonArray("createNewHeroes").size(),
                "Wrong size array while createNewHeroes");

    }

    @Test
    public void testMutationWithCollectionTransformationInput() {
        JsonObject data = executeAndGetData(MUTATION_COMPLEX_TRANSFORMATION_COLLECTION);

        assertFalse(data.isNull("createNewHero"), "createNewHero should not be null");

        assertFalse(data.getJsonObject("createNewHero").isNull("name"), "name should not be null");
        assertEquals("Black Panther", data.getJsonObject("createNewHero").getString("name"),
                "Wrong name while createNewHero hero");

        assertFalse(data.getJsonObject("createNewHero").isNull("agesOfKids"), "agesOfKids should not be null");

        JsonArray jsonArray = data.getJsonObject("createNewHero").getJsonArray("agesOfKids");
        assertEquals(2, jsonArray.size(),
                "Wrong size agesOfKids while createNewHero member");

        Object[] receivedKids = new Object[] { jsonArray.getJsonString(0).toString(), jsonArray.getJsonString(1).toString() };
        Object[] expectedKids = new Object[] { "\"3 years\"", "\"5 years\"" };

        assertArrayEquals(expectedKids, receivedKids);

    }

    @Test
    public void testMutationWithScalarDateInput() {
        JsonObject data = executeAndGetData(MUTATION_SCALAR_DATE_INPUT);

        assertFalse(data.isNull("startPatrolling"), "startPatrolling should not be null");

        assertFalse(data.getJsonObject("startPatrolling").isNull("name"), "name should not be null");
        assertEquals("Starlord", data.getJsonObject("startPatrolling").getString("name"),
                "Wrong name while startPatrolling");

        assertFalse(data.getJsonObject("startPatrolling").isNull("patrolStartTime"),
                "patrolStartTime should not be null");
        assertEquals("20:00", data.getJsonObject("startPatrolling").getString("patrolStartTime"),
                "Wrong time while patrolStartTime");
    }

    @Test
    public void testMutationWithScalarNumberInput() {
        JsonObject data = executeAndGetData(MUTATION_SCALAR_NUMBER_INPUT);

        assertFalse(data.isNull("idNumber"), "idNumber should not be null");

        assertFalse(data.getJsonObject("idNumber").isNull("name"), "name should not be null");
        assertEquals("Starlord", data.getJsonObject("idNumber").getString("name"),
                "Wrong name while idNumber");

        assertFalse(data.getJsonObject("idNumber").isNull("idNumber"),
                "idNumber should not be null");
        assertEquals("ID-77777777", data.getJsonObject("idNumber").getString("idNumber"),
                "Wrong idNumber while idNumber");
    }

    @Test
    public void testMutationWithInvalidTimeInput() {
        JsonArray errors = executeAndGetError(MUTATION_INVALID_TIME_SCALAR);

        assertEquals(1, errors.size(),
                "Wrong size for errors while startPatrolling with wrong date");

        JsonObject error = errors.getJsonObject(0);

        assertFalse(error.isNull("message"), "message should not be null");

        assertEquals(
                "Validation error of type WrongType: argument 'time' with value 'StringValue{value='Today'}' is not a valid 'Time' @ 'startPatrolling'",
                error.getString("message"),
                "Wrong error message while startPatrolling with wrong date");
    }

    @Test
    public void testMutationWithInvalidNumberInput() {
        JsonArray errors = executeAndGetError(MUTATION_INVALID_NUMBER_SCALAR);

        assertEquals(1, errors.size(),
                "Wrong size for errors while updateItemPowerLevel with wrong numner");

        JsonObject error = errors.getJsonObject(0);

        assertFalse(error.isNull("message"), "message should not be null");

        assertEquals(
                "Validation error of type WrongType: argument 'powerLevel' with value 'StringValue{value='Unlimited'}' is not a valid 'Int' @ 'updateItemPowerLevel'",
                error.getString("message"),
                "Wrong error message while updateItemPowerLevel with wrong number");
    }

    @Test
    public void testDefaultTimeScalarFormat() {
        JsonObject data = executeAndGetData(QUERY_DEFAULT_TIME_FORMAT);

        assertFalse(data.isNull("testScalarsInPojo"), "testScalarsInPojo should not be null");

        assertFalse(data.getJsonObject("testScalarsInPojo").isNull("timeObject"), "timeObject should not be null");
        assertEquals("11:46:34.263", data.getJsonObject("testScalarsInPojo").getString("timeObject"),
                "Wrong wrong time format");

    }

    @Test
    public void testInputWithDifferentNameOnInputAndType() {
        JsonObject data = executeAndGetData(MUTATION_NAME_DIFF_ON_INPUT_AND_TYPE);

        assertFalse(data.isNull("createNewHero"), "createNewHero should not be null");

        assertFalse(data.getJsonObject("createNewHero").isNull("sizeOfTShirt"), "sizeOfTShirt should not be null");
        assertEquals("XL", data.getJsonObject("createNewHero").getString("sizeOfTShirt"),
                "Wrong sizeOfTShirt ");

    }

    private JsonObject executeAndGetData(String graphQL) {
        JsonObject result = executionService.execute(toJsonObject(graphQL));

        String prettyData = getPrettyJson(result);
        LOG.info(prettyData);

        return result.getJsonObject(DATA);
    }

    private JsonArray executeAndGetError(String graphQL) {
        JsonObject result = executionService.execute(toJsonObject(graphQL));

        String prettyData = getPrettyJson(result);
        LOG.info(prettyData);

        return result.getJsonArray(ERRORS);
    }

    private JsonObject toJsonObject(String graphQL) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("query", graphQL);
        return builder.build();
    }

    private Config getGraphQLConfig() {
        return new Config() {
            @Override
            public boolean isPrintDataFetcherException() {
                return true;
            }
        };
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

    // This test a cenario where the inputfield and typefield is named different
    private static final String MUTATION_NAME_DIFF_ON_INPUT_AND_TYPE = "mutation inputFieldWithAnotherName {\n" +
            "   createNewHero (hero: {\n" +
            "    realName: \"Steven Rogers\"\n" +
            "    name: \"Captain America\"\n" +
            "    dateOfLastCheckin: \"09/25/2019\"\n" +
            "    patrolStartTime: \"13:00\"\n" +
            "    timeOfLastBattle: \"09:43:23 21-08-2019\"\n" +
            "    tshirtSize: XL\n" +
            "  }) {\n" +
            "    name\n" +
            "    dateOfLastCheckin\n" +
            "    patrolStartTime\n" +
            "    sizeOfTShirt\n" +
            "  } \n" +
            "}";

    // This test the default time format
    private static final String QUERY_DEFAULT_TIME_FORMAT = "{\n" +
            "  testScalarsInPojo {\n" +
            "    timeObject\n" +
            "    anotherTimeObject\n" +
            "  }\n" +
            "}";

    // This test invalid number scalars as input (expecting an error)
    private static final String MUTATION_INVALID_NUMBER_SCALAR = "mutation increaseIronManSuitPowerTooHigh {\n" +
            "  updateItemPowerLevel(itemID:1001, powerLevel:\"Unlimited\") {\n" +
            "    id\n" +
            "    name\n" +
            "    powerLevel\n" +
            "  }\n" +
            "}";

    // This test invalid date scalars as input (expecting an error)
    private static final String MUTATION_INVALID_TIME_SCALAR = "mutation invalidPatrollingDate {\n" +
            "  startPatrolling(name:\"Starlord\", time:\"Today\") {\n" +
            "    name\n" +
            "    patrolStartTime\n" +
            "  }\n" +
            "}";

    // This test number Scalars as input to operations
    private static final String MUTATION_SCALAR_NUMBER_INPUT = "mutation setHeroIdNumber {\n" +
            "  idNumber(name:\"Starlord\", id:77777777) {\n" +
            "    name\n" +
            "    idNumber\n" +
            "  }\n" +
            "}";

    // This test date Scalars as input to operations
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
    private static final Class heroFinder;
    private static final Class heroDatabase;
    private static final Class sidekickDatabase;
    private static final Class heroLocator;
    private static final Class scalarTestApi;

    private static final Map<String, Object> JSON_PROPERTIES = new HashMap<>(1);

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

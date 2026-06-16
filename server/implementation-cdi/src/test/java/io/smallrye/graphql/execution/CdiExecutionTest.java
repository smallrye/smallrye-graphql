package io.smallrye.graphql.execution;

import static io.smallrye.graphql.JsonProviderHolder.JSON_PROVIDER;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import org.eclipse.microprofile.graphql.tck.apps.basic.api.ScalarTestApi;
import org.eclipse.microprofile.graphql.tck.apps.superhero.api.HeroFinder;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.jboss.weld.junit5.auto.ActivateScopes;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.smallrye.graphql.cdi.producer.GraphQLProducer;
import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.spi.LookupService;

/**
 * Test a basic query
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@EnableAutoWeld
@ActivateScopes({ RequestScoped.class, ApplicationScoped.class })
@AddBeanClasses({ ScalarTestApi.class, HeroFinder.class })
public class CdiExecutionTest {
    private static final Logger LOG = Logger.getLogger(CdiExecutionTest.class.getName());

    @Inject
    GraphQLProducer graphQLProducer;

    @BeforeEach
    public void init() {
        IndexView index = Indexer.getTCKIndex();
        Schema schema = SchemaBuilder.build(index);
        graphQLProducer.initialize(schema);
    }

    @Test
    public void testBasicQuery() {
        ObjectNode data = executeAndGetData(GET_HERO);

        ObjectNode superHero = (ObjectNode) data.get("superHero");

        assertNotNull(superHero);

        assertTrue(superHero.has("name") && !superHero.get("name").isNull(), "name should not be null");
        assertEquals("Iron Man", superHero.get("name").asText());

        assertTrue(superHero.has("primaryLocation") && !superHero.get("primaryLocation").isNull(),
                "primaryLocation should not be null");
        assertEquals("Los Angeles, CA", superHero.get("primaryLocation").asText());

        assertTrue(superHero.has("realName") && !superHero.get("realName").isNull(), "realName should not be null");
        assertEquals("Tony Stark", superHero.get("realName").asText());

        // To test @Source
        assertTrue(superHero.has("currentLocation") && !superHero.get("currentLocation").isNull(),
                "currentLocation should not be null (@Source not working)");
        assertEquals("Wachovia", superHero.get("currentLocation").asText());

        // To test @Source with extra default parameter
        assertTrue(superHero.has("secretToken") && !superHero.get("secretToken").isNull(),
                "secretToken should not be null (@Source with extra parameter not working)");
        assertTrue(
                ((ObjectNode) superHero.get("secretToken")).get("value").asText()
                        .startsWith("********-****-****-****-********"));

        // To test Number formatting (on return object fields)
        assertTrue(superHero.has("idNumber") && !superHero.get("idNumber").isNull(), "idNumber should not be null");
        assertEquals("ID-12345678", superHero.get("idNumber").asText());

        // To test Date formatting (on return object fields)
        assertTrue(superHero.has("dateOfLastCheckin") && !superHero.get("dateOfLastCheckin").isNull(),
                "Transformation on Date not working");
        assertEquals("09/09/2019", superHero.get("dateOfLastCheckin").asText());

        // To test DateTime formatting (on return object fields)
        assertTrue(superHero.has("timeOfLastBattle") && !superHero.get("timeOfLastBattle").isNull(),
                "Transformation on DateTime not working");
        assertEquals("08:30:01 06-09-2019", superHero.get("timeOfLastBattle").asText());

        // To test Time formatting (on return object fields)
        assertTrue(superHero.has("patrolStartTime") && !superHero.get("patrolStartTime").isNull(),
                "Transformation on Time not working");
        assertEquals("08:00", superHero.get("patrolStartTime").asText());

    }

    @Test
    public void testDateTransformationOnQuery() {
        ObjectNode data = executeAndGetData(TRANSFORMED_DATE);

        assertTrue(data.has("transformedDate") && !data.get("transformedDate").isNull(),
                "transformedDate should not be null");
        String transformedDate = data.get("transformedDate").asText();

        assertEquals("16 Aug 2016", transformedDate, "Date transformation on Query not working");
    }

    @Test
    public void testNumberTransformationOnMutation() {
        ObjectNode data = executeAndGetData(TRANSFORMED_NUMBER);

        assertTrue(data.has("transformedNumber") && !data.get("transformedNumber").isNull(),
                "transformedNumber should not be null");
        assertEquals("number 345", data.get("transformedNumber").asText(), "Number transformation on Mutation not working");
    }

    @Test
    public void testNumberTransformationOnArgument() {
        ObjectNode data = executeAndGetData(TRANSFORMED_ARGUMENT);

        assertTrue(data.has("idNumberWithCorrectFormat") && !data.get("idNumberWithCorrectFormat").isNull(),
                "idNumberWithCorrectFormat should not be null");
        ObjectNode idNumberObj = (ObjectNode) data.get("idNumberWithCorrectFormat");
        assertTrue(idNumberObj.has("idNumber") && !idNumberObj.get("idNumber").isNull(), "idNumber should not be null");
        assertEquals("ID-88888888", idNumberObj.get("idNumber").asText(),
                "Number transformation on Argument not working");

    }

    @Test
    public void testBasicMutation() {
        ObjectNode data = executeAndGetData(MUTATION_BASIC);

        assertTrue(data.has("addHeroToTeam") && !data.get("addHeroToTeam").isNull(), "addHeroToTeam should not be null");

        ObjectNode addHeroToTeam = (ObjectNode) data.get("addHeroToTeam");
        assertTrue(addHeroToTeam.has("name") && !addHeroToTeam.get("name").isNull(), "name should not be null");
        assertEquals("Avengers", addHeroToTeam.get("name").asText(),
                "Wrong team name while adding member");

        assertTrue(addHeroToTeam.has("members") && !addHeroToTeam.get("members").isNull(), "members should not be null");
        assertEquals(4, ((ArrayNode) addHeroToTeam.get("members")).size(),
                "Wrong team size while adding member");

    }

    @Test
    public void testMutationWithObjectArgument() {
        ObjectNode data = executeAndGetData(MUTATION_COMPLEX);

        assertTrue(data.has("createNewHero") && !data.get("createNewHero").isNull(), "createNewHero should not be null");

        ObjectNode createNewHero = (ObjectNode) data.get("createNewHero");
        assertTrue(createNewHero.has("name") && !createNewHero.get("name").isNull(), "name should not be null");
        assertEquals("Captain America", createNewHero.get("name").asText(),
                "Wrong name while adding hero");

        assertTrue(createNewHero.has("superPowers") && !createNewHero.get("superPowers").isNull(),
                "superPowers should not be null");
        assertEquals(2, ((ArrayNode) createNewHero.get("superPowers")).size(),
                "Wrong size superPowers while adding member");

    }

    @Test
    public void testMutationScalarJavaMapping() {
        ObjectNode data = executeAndGetData(MUTATION_SCALAR_MAPPING);

        assertTrue(data.has("provisionHero") && !data.get("provisionHero").isNull(), "provisionHero should not be null");

        ObjectNode provisionHero = (ObjectNode) data.get("provisionHero");
        assertTrue(provisionHero.has("name") && !provisionHero.get("name").isNull(), "name should not be null");
        assertEquals("Starlord", provisionHero.get("name").asText(),
                "Wrong name while provisioning hero");

        assertTrue(provisionHero.has("equipment") && !provisionHero.get("equipment").isNull(),
                "equipment should not be null");
        assertEquals(1, ((ArrayNode) provisionHero.get("equipment")).size(),
                "Wrong size equipment while provisioning member");

    }

    @Test
    public void testMutationWithComplexDefault() {
        ObjectNode data = executeAndGetData(MUTATION_COMPLEX_DEFAULT);

        assertTrue(data.has("provisionHero") && !data.get("provisionHero").isNull(), "provisionHero should not be null");

        ObjectNode provisionHero = (ObjectNode) data.get("provisionHero");
        assertTrue(provisionHero.has("name") && !provisionHero.get("name").isNull(), "name should not be null");
        assertEquals("Spider Man", provisionHero.get("name").asText(),
                "Wrong name while provisioning hero");

        assertTrue(provisionHero.has("equipment") && !provisionHero.get("equipment").isNull(),
                "equipment should not be null");
        assertEquals(1, ((ArrayNode) provisionHero.get("equipment")).size(),
                "Wrong size equipment while provisioning member");

    }

    @Test
    public void testMutationWithArrayInput() {
        ObjectNode data = executeAndGetData(MUTATION_COMPLEX_ARRAY);

        assertTrue(data.has("createNewHeroesWithArray") && !data.get("createNewHeroesWithArray").isNull(),
                "createNewHeroesWithArray should not be null");

        assertEquals(1, ((ArrayNode) data.get("createNewHeroesWithArray")).size(),
                "Wrong size array while createNewHeroesWithArray");

    }

    @Test
    public void testMutationWithCollectionInput() {
        ObjectNode data = executeAndGetData(MUTATION_COMPLEX_COLLECTION);

        assertTrue(data.has("createNewHeroes") && !data.get("createNewHeroes").isNull(),
                "createNewHeroes should not be null");

        assertEquals(1, ((ArrayNode) data.get("createNewHeroes")).size(),
                "Wrong size array while createNewHeroes");

    }

    @Test
    public void testMutationWithCollectionTransformationInput() {
        ObjectNode data = executeAndGetData(MUTATION_COMPLEX_TRANSFORMATION_COLLECTION);

        assertTrue(data.has("createNewHero") && !data.get("createNewHero").isNull(), "createNewHero should not be null");

        ObjectNode createNewHero = (ObjectNode) data.get("createNewHero");
        assertTrue(createNewHero.has("name") && !createNewHero.get("name").isNull(), "name should not be null");
        assertEquals("Black Panther", createNewHero.get("name").asText(),
                "Wrong name while createNewHero hero");

        assertTrue(createNewHero.has("agesOfKids") && !createNewHero.get("agesOfKids").isNull(),
                "agesOfKids should not be null");

        ArrayNode jsonArray = (ArrayNode) createNewHero.get("agesOfKids");
        assertEquals(2, jsonArray.size(),
                "Wrong size agesOfKids while createNewHero member");

        Object[] receivedKids = new Object[] { "\"" + jsonArray.get(0).asText() + "\"",
                "\"" + jsonArray.get(1).asText() + "\"" };
        Object[] expectedKids = new Object[] { "\"3 years\"", "\"5 years\"" };

        assertArrayEquals(expectedKids, receivedKids);

    }

    @Test
    public void testMutationWithScalarDateInput() {
        ObjectNode data = executeAndGetData(MUTATION_SCALAR_DATE_INPUT);

        assertTrue(data.has("startPatrolling") && !data.get("startPatrolling").isNull(),
                "startPatrolling should not be null");

        ObjectNode startPatrolling = (ObjectNode) data.get("startPatrolling");
        assertTrue(startPatrolling.has("name") && !startPatrolling.get("name").isNull(), "name should not be null");
        assertEquals("Starlord", startPatrolling.get("name").asText(),
                "Wrong name while startPatrolling");

        assertTrue(startPatrolling.has("patrolStartTime") && !startPatrolling.get("patrolStartTime").isNull(),
                "patrolStartTime should not be null");
        assertEquals("20:00", startPatrolling.get("patrolStartTime").asText(),
                "Wrong time while patrolStartTime");
    }

    @Test
    public void testMutationWithScalarNumberInput() {
        ObjectNode data = executeAndGetData(MUTATION_SCALAR_NUMBER_INPUT);

        assertTrue(data.has("idNumber") && !data.get("idNumber").isNull(), "idNumber should not be null");

        ObjectNode idNumber = (ObjectNode) data.get("idNumber");
        assertTrue(idNumber.has("name") && !idNumber.get("name").isNull(), "name should not be null");
        assertEquals("Starlord", idNumber.get("name").asText(),
                "Wrong name while idNumber");

        assertTrue(idNumber.has("idNumber") && !idNumber.get("idNumber").isNull(),
                "idNumber should not be null");
        assertEquals("ID-77777777", idNumber.get("idNumber").asText(),
                "Wrong idNumber while idNumber");
    }

    @Test
    public void testMutationWithInvalidTimeInput() {
        ArrayNode errors = executeAndGetError(MUTATION_INVALID_TIME_SCALAR);

        assertEquals(1, errors.size(),
                "Wrong size for errors while startPatrolling with wrong date");

        ObjectNode error = (ObjectNode) errors.get(0);

        assertTrue(error.has("message") && !error.get("message").isNull(), "message should not be null");

        assertEquals(
                "argument 'time' with value 'StringValue{value='Today'}' is not a valid 'Time'",
                error.get("message").asText(),
                "Wrong error message while startPatrolling with wrong date");
    }

    @Test
    public void testMutationWithInvalidNumberInput() {
        ArrayNode errors = executeAndGetError(MUTATION_INVALID_NUMBER_SCALAR);

        assertEquals(1, errors.size(),
                "Wrong size for errors while updateItemPowerLevel with wrong number");

        ObjectNode error = (ObjectNode) errors.get(0);

        assertTrue(error.has("message") && !error.get("message").isNull(), "message should not be null");

        assertEquals(
                "Validation error (WrongType@[updateItemPowerLevel]) : argument 'powerLevel' with value 'StringValue{value='Unlimited'}' is not a valid 'Int' - SRGQL000022: Can not parse a number from [StringValue{value='Unlimited'}]",
                error.get("message").asText(),
                "Wrong error message while updateItemPowerLevel with wrong number");
    }

    @Test
    public void testParsingInvalidNumberScalar() {
        ArrayNode errors = executeAndGetError(MUTATION_INVALID_INTEGER_SCALAR);

        assertEquals(1, errors.size(),
                "Wrong size for errors while updateItemPowerLevel with wrong number");

        ObjectNode error = (ObjectNode) errors.get(0);

        assertTrue(error.has("message") && !error.get("message").isNull(), "message should not be null");

        assertEquals(
                "Validation error (WrongType@[updateItemPowerLevel]) : argument 'powerLevel' with value 'StringValue{value='3.14'}' is not a valid 'Int' - SRGQL000021: Can not parse a integer from [StringValue{value='3.14'}]",
                error.get("message").asText(),
                "Wrong error message while updateItemPowerLevel with wrong number");
    }

    @Test
    public void testDefaultTimeScalarFormat() {
        ObjectNode data = executeAndGetData(QUERY_DEFAULT_TIME_FORMAT);

        assertTrue(data.has("testScalarsInPojo") && !data.get("testScalarsInPojo").isNull(),
                "testScalarsInPojo should not be null");

        ObjectNode testScalarsInPojo = (ObjectNode) data.get("testScalarsInPojo");
        assertTrue(testScalarsInPojo.has("timeObject") && !testScalarsInPojo.get("timeObject").isNull(),
                "timeObject should not be null");
        assertEquals("11:46:34.263", testScalarsInPojo.get("timeObject").asText(),
                "Wrong wrong time format");
    }

    @Test
    public void testInputWithDifferentNameOnInputAndType() {
        ObjectNode data = executeAndGetData(MUTATION_NAME_DIFF_ON_INPUT_AND_TYPE);

        assertTrue(data.has("createNewHero") && !data.get("createNewHero").isNull(), "createNewHero should not be null");

        ObjectNode createNewHero = (ObjectNode) data.get("createNewHero");
        assertTrue(createNewHero.has("sizeOfTShirt") && !createNewHero.get("sizeOfTShirt").isNull(),
                "sizeOfTShirt should not be null");
        assertEquals("XL", createNewHero.get("sizeOfTShirt").asText(),
                "Wrong sizeOfTShirt ");

    }

    private ObjectNode executeAndGetData(String graphQL) {
        JsonObjectResponseWriter jor = new JsonObjectResponseWriter(graphQL);
        ExecutionService executionService = LookupService.get().getInstance(ExecutionService.class).get();
        executionService.executeSync(toJsonObject(graphQL), jor);

        ExecutionResponse result = jor.getExecutionResponse();

        String prettyData = result.getExecutionResultAsString();
        LOG.info(prettyData);

        return (ObjectNode) result.getExecutionResultAsJsonObject().get(DATA);
    }

    private ArrayNode executeAndGetError(String graphQL) {
        JsonObjectResponseWriter jor = new JsonObjectResponseWriter(graphQL);
        ExecutionService executionService = LookupService.get().getInstance(ExecutionService.class).get();
        executionService.executeSync(toJsonObject(graphQL), jor);
        ExecutionResponse result = jor.getExecutionResponse();

        String prettyData = result.getExecutionResultAsString();
        LOG.info(prettyData);

        return (ArrayNode) result.getExecutionResultAsJsonObject().get(ERRORS);
    }

    private JsonObject toJsonObject(String graphQL) {
        JsonObjectBuilder builder = JSON_PROVIDER.createObjectBuilder();
        builder.add("query", graphQL);
        return builder.build();
    }

    private static final String DATA = "data";
    private static final String ERRORS = "errors";

    // This test a scenario where the inputfield and typefield is named different
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

    // This test invalid integer (as a float) scalar as input (expecting an error)
    private static final String MUTATION_INVALID_INTEGER_SCALAR = "mutation increaseIronManSuitPowerTooHigh {\n" +
            "  updateItemPowerLevel(itemID:1001, powerLevel:\"3.14\") {\n" +
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

}

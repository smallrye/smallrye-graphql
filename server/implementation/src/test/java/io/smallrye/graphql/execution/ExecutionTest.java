package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.*;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.test.TestSourceConfiguration;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Test a basic query
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionTest extends ExecutionTestBase {

    @Test
    public void testBasicQuery() {
        ObjectNode data = executeAndGetData(TEST_QUERY);

        ObjectNode testObject = (ObjectNode) data.get("testObject");

        assertNotNull(testObject);

        assertFalse(testObject.has("name") && testObject.get("name").isNull(), "name should not be null");
        assertEquals("Phillip", testObject.get("name").asText());

        assertFalse(testObject.has("id") && testObject.get("id").isNull(), "id should not be null");

        // Testing source
        assertFalse(testObject.has("timestamp") && testObject.get("timestamp").isNull(), "timestamp should not be null");
        assertFalse(((ObjectNode) testObject.get("timestamp")).has("value")
                && ((ObjectNode) testObject.get("timestamp")).get("value").isNull(), "timestamp value should not be null");

    }

    @Test
    public void testBasicListQuery() {
        ObjectNode data = executeAndGetData(TEST_LIST_QUERY);

        ArrayNode testObjects = (ArrayNode) data.get("testObjects");

        assertNotNull(testObjects);
        assertEquals(2, testObjects.size());
        ObjectNode testObject = (ObjectNode) testObjects.get(0);
        assertNotNull(testObject);

        assertFalse(testObject.has("name") && testObject.get("name").isNull(), "name should not be null");
        assertEquals("Phillip", testObject.get("name").asText());

        assertFalse(testObject.has("id") && testObject.get("id").isNull(), "id should not be null");

        // Testing batch
        assertFalse(testObject.has("timestamp") && testObject.get("timestamp").isNull(), "timestamp should not be null");
        assertFalse(((ObjectNode) testObject.get("timestamp")).has("value")
                && ((ObjectNode) testObject.get("timestamp")).get("value").isNull(), "timestamp value should not be null");
    }

    @Test
    public void testBatchSourceConfigurationQuery() {
        ObjectNode data = executeAndGetData(TEST_BATCH_SOURCE_CONFIGURATION_QUERY);

        Boolean active1 = ((ObjectNode) ((ObjectNode) ((ObjectNode) ((ArrayNode) data.get("objectsWithConfig1")).get(0))
                .get("configuredSources")).get("configuration")).get("active").asBoolean();
        Boolean active2 = ((ObjectNode) ((ObjectNode) ((ObjectNode) ((ArrayNode) data.get("objectsWithConfig2")).get(0))
                .get("configuredSources")).get("configuration")).get("active").asBoolean();

        var state1 = TestSourceConfiguration.TestSourceState.valueOf(((ObjectNode) ((ObjectNode) ((ObjectNode) ((ArrayNode) data
                .get("objectsWithConfig1")).get(0)).get("configuredSources")).get("configuration")).get("state").asText());
        var state2 = TestSourceConfiguration.TestSourceState.valueOf(((ObjectNode) ((ObjectNode) ((ObjectNode) ((ArrayNode) data
                .get("objectsWithConfig2")).get(0)).get("configuredSources")).get("configuration")).get("state").asText());

        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            softly.assertThat(active1).isNotEqualTo(active2);
            softly.assertThat(state1).isNotEqualTo(state2);
        }
    }

    private static final String TEST_QUERY = "{\n" +
            "  testObject(yourname:\"Phillip\") {\n" +
            "    id\n" +
            "    name\n" +
            "    timestamp {\n" +
            "       value\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String TEST_LIST_QUERY = "{\n" +
            "  testObjects {\n" +
            "    id\n" +
            "    name\n" +
            "    timestamp {\n" +
            "       value\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String TEST_BATCH_SOURCE_CONFIGURATION_QUERY = "{\n" +
            "  objectsWithConfig1: testObjectsPersisted {\n" +
            "    id\n" +
            "    name\n" +
            "    configuredSources(configuration: {active:true, state: PENDING}) {\n" +
            "       configuration{\n" +
            "           active\n" +
            "           state\n" +
            "       }\n" +
            "    }\n" +
            "  }\n" +
            "  objectsWithConfig2: testObjectsPersisted {\n" +
            "    id\n" +
            "    name\n" +
            "    configuredSources(configuration: {active:false, state: IN_PROGRESS}) {\n" +
            "       configuration{\n" +
            "           active\n" +
            "           state\n" +
            "       }\n" +
            "    }\n" +
            "  }\n" +
            "}";

}

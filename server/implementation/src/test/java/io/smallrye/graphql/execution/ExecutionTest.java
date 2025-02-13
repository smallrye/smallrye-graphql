package io.smallrye.graphql.execution;

import io.smallrye.graphql.test.TestSourceConfiguration;
import jakarta.json.*;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.eclipse.parsson.JsonPointerImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test a basic query
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ExecutionTest extends ExecutionTestBase {

    @Test
    public void testBasicQuery() {
        JsonObject data = executeAndGetData(TEST_QUERY);

        JsonObject testObject = data.getJsonObject("testObject");

        assertNotNull(testObject);

        assertFalse(testObject.isNull("name"), "name should not be null");
        assertEquals("Phillip", testObject.getString("name"));

        assertFalse(testObject.isNull("id"), "id should not be null");

        // Testing source
        assertFalse(testObject.isNull("timestamp"), "timestamp should not be null");
        assertFalse(testObject.get("timestamp").asJsonObject().isNull("value"), "timestamp value should not be null");

    }

    @Test
    public void testBasicListQuery() {
        JsonObject data = executeAndGetData(TEST_LIST_QUERY);

        JsonArray testObjects = data.getJsonArray("testObjects");

        assertNotNull(testObjects);
        assertEquals(2, testObjects.size());
        JsonObject testObject = testObjects.getJsonObject(0);
        assertNotNull(testObject);

        assertFalse(testObject.isNull("name"), "name should not be null");
        assertEquals("Phillip", testObject.getString("name"));

        assertFalse(testObject.isNull("id"), "id should not be null");

        // Testing batch
        assertFalse(testObject.isNull("timestamp"), "timestamp should not be null");
        assertFalse(testObject.get("timestamp").asJsonObject().isNull("value"), "timestamp value should not be null");
    }

    @Test
    public void testBatchSourceConfigurationQuery() {
        JsonObject data = executeAndGetData(TEST_BATCH_SOURCE_CONFIGURATION_QUERY);

        JsonPointer active1Pointer = new JsonPointerImpl("/objectsWithConfig1/0/configuredSources/configuration/active");
        Boolean active1 = Boolean.valueOf(active1Pointer.getValue(data).toString());
        JsonPointer active2Pointer = new JsonPointerImpl("/objectsWithConfig2/0/configuredSources/configuration/active");
        Boolean active2 = Boolean.valueOf(active2Pointer.getValue(data).toString());

        JsonPointer state1Pointer = new JsonPointerImpl("/objectsWithConfig1/0/configuredSources/configuration/state");
        var state1 = TestSourceConfiguration.TestSourceState.valueOf(((JsonString) state1Pointer.getValue(data)).getString());
        JsonPointer state2Pointer = new JsonPointerImpl("/objectsWithConfig2/0/configuredSources/configuration/state");
        var state2 = TestSourceConfiguration.TestSourceState.valueOf(((JsonString) state2Pointer.getValue(data)).getString());

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

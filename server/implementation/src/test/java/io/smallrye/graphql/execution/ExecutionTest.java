package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.junit.jupiter.api.Test;

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

}

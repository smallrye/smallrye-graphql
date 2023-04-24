package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.json.JsonObject;

import org.junit.jupiter.api.Test;

/**
 * Test against generics
 *
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class ExecutionGenericsTest extends ExecutionTestBase {

    @Test
    public void testQueryGeneric1() {
        JsonObject data = executeAndGetData(TEST_QUERY_GENERIC_1);

        JsonObject testObject = data.getJsonObject("generic1");

        assertNotNull(testObject);

        assertFalse(testObject.isNull("name"), "name should not be null");
        assertEquals("my name", testObject.getString("name"));

        assertFalse(testObject.isNull("param1"), "param1 should not be null");
        assertEquals("my param 1", testObject.getString("param1"));

    }

    @Test
    public void testQueryGeneric2() {
        JsonObject data = executeAndGetData(TEST_QUERY_GENERIC_2);

        JsonObject testObject = data.getJsonObject("generic2");

        assertNotNull(testObject);

        assertFalse(testObject.isNull("name"), "name should not be null");
        assertEquals("my name", testObject.getString("name"));

        assertFalse(testObject.isNull("param1"), "param1 should not be null");
        assertEquals(22, testObject.getInt("param1"));

    }

    private static final String TEST_QUERY_GENERIC_1 = "{\n" +
            "  generic1 {\n" +
            "    param1\n" +
            "    name\n" +
            "  }\n" +
            "}";

    private static final String TEST_QUERY_GENERIC_2 = "{\n" +
            "  generic2 {\n" +
            "    param1\n" +
            "    name\n" +
            "  }\n" +
            "}";

}

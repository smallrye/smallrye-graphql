package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.node.ObjectNode;

/**
 * Test against generics
 *
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class ExecutionGenericsTest extends ExecutionTestBase {

    @Test
    public void testQueryGeneric1() {
        ObjectNode data = executeAndGetData(TEST_QUERY_GENERIC_1);

        ObjectNode testObject = (ObjectNode) data.get("generic1");

        assertNotNull(testObject);

        assertFalse(testObject.has("name") && testObject.get("name").isNull(), "name should not be null");
        assertEquals("my name", testObject.get("name").asText());

        assertFalse(testObject.has("param1") && testObject.get("param1").isNull(), "param1 should not be null");
        assertEquals("my param 1", testObject.get("param1").asText());

    }

    @Test
    public void testQueryGeneric2() {
        ObjectNode data = executeAndGetData(TEST_QUERY_GENERIC_2);

        ObjectNode testObject = (ObjectNode) data.get("generic2");

        assertNotNull(testObject);

        assertFalse(testObject.has("name") && testObject.get("name").isNull(), "name should not be null");
        assertEquals("my name", testObject.get("name").asText());

        assertFalse(testObject.has("param1") && testObject.get("param1").isNull(), "param1 should not be null");
        assertEquals(22, testObject.get("param1").asInt());

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

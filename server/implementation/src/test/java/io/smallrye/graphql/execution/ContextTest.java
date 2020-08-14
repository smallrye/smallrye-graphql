package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.json.JsonObject;

import org.junit.jupiter.api.Test;

/**
 * Test the context
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ContextTest extends ExecutionTestBase {

    @Test
    public void testContext() {
        JsonObject data = executeAndGetData(TEST_QUERY);

        JsonObject testObject = data.getJsonObject("testContext");

        assertNotNull(testObject);

        assertFalse(testObject.isNull("executionId"), "executionId should not be null");

        assertFalse(testObject.isNull("path"), "path should not be null");
        assertEquals("/testContext", testObject.getString("path"));

        assertFalse(testObject.isNull("query"), "query should not be null");
    }

    private static final String TEST_QUERY = "{\n" +
            "  testContext {\n" +
            "    executionId\n" +
            "    path\n" +
            "    query\n" +
            "  }\n" +
            "}";

}

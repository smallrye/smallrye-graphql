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
    public void testBasicQuery() {
        JsonObject data = executeAndGetData(TEST_QUERY);

        JsonObject testObject = data.getJsonObject("testObject");

        assertNotNull(testObject);

        assertFalse(testObject.isNull("name"), "name should not be null");
        assertEquals("Phillip", testObject.getString("name"));

        JsonObject timestamp = testObject.getJsonObject("timestamp");
        assertNotNull(timestamp);
        assertNotNull(timestamp.getString("value"));

        assertFalse(testObject.isNull("id"), "id should not be null");

    }

    private static final String TEST_QUERY = "{\n" +
            "  testObject(yourname:\"Phillip\") {\n" +
            "    id\n" +
            "    name\n" +
            "    timestamp(indicator:\"xyz\"){" +
            "       value" +
            "    }" +
            "    amounts{" +
            "       amount" +
            "    }" +
            "  }\n" +
            "}";

}

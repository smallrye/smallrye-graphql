package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.json.JsonObject;

import org.junit.jupiter.api.Test;

import graphql.language.Document;
import io.smallrye.graphql.execution.event.TestEventingService;

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

    @Test
    public void testUnwrapDocument() {
        TestEventingService.reset();

        JsonObject data = executeAndGetData(TEST_QUERY);
        Document document = TestEventingService.beforeExecuteContext.unwrap(Document.class);
        assertNotNull(document, "unwrapped document was null");

        TestEventingService.reset();
    }

    private static final String TEST_QUERY = "{\n" +
            "  testContext {\n" +
            "    executionId\n" +
            "    path\n" +
            "    query\n" +
            "  }\n" +
            "}";

}

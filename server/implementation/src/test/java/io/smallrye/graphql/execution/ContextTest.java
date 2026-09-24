package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import graphql.language.Document;
import io.smallrye.graphql.execution.event.TestEventingService;
import tools.jackson.databind.node.ObjectNode;

/**
 * Test the context
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ContextTest extends ExecutionTestBase {

    @Test
    public void testContext() {
        ObjectNode data = executeAndGetData(TEST_QUERY);

        ObjectNode testObject = (ObjectNode) data.get("testContext");

        assertNotNull(testObject);

        assertFalse(testObject.has("executionId") && testObject.get("executionId").isNull(),
                "executionId should not be null");

        assertFalse(testObject.has("path") && testObject.get("path").isNull(), "path should not be null");
        assertEquals("/testContext", testObject.get("path").asText());

        assertFalse(testObject.has("query") && testObject.get("query").isNull(), "query should not be null");
    }

    @Test
    public void testUnwrapDocument() {
        TestEventingService.reset();

        ObjectNode data = executeAndGetData(TEST_QUERY);
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

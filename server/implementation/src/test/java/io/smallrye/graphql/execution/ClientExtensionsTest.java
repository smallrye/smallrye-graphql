package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import graphql.ExecutionInput;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.event.TestEventingService;

/**
 * Test that the extensions a client sends with a request reach the graphql-java {@link ExecutionInput}
 */
public class ClientExtensionsTest extends ExecutionTestBase {

    @AfterEach
    void tearDown() {
        TestEventingService.reset();
    }

    @Test
    public void testClientExtensionsReachExecutionInput() {
        JsonObject request = Json.createObjectBuilder()
                .add("query", TEST_QUERY)
                .add("extensions", Json.createObjectBuilder().add("probe", "x"))
                .build();

        ExecutionInput executionInput = execute(request);

        assertEquals(Map.of("probe", "x"), executionInput.getExtensions());
    }

    @Test
    public void testNoClientExtensionsGiveEmptyExecutionInputExtensions() {
        JsonObject request = Json.createObjectBuilder()
                .add("query", TEST_QUERY)
                .build();

        ExecutionInput executionInput = execute(request);

        assertNotNull(executionInput.getExtensions());
        assertTrue(executionInput.getExtensions().isEmpty());
    }

    private ExecutionInput execute(JsonObject request) {
        JsonObjectResponseWriter writer = new JsonObjectResponseWriter(request);
        executionService.executeSync(request, writer);
        SmallRyeContext context = (SmallRyeContext) TestEventingService.beforeExecuteContext;
        assertNotNull(context);
        return context.getExecutionInput();
    }

    private static final String TEST_QUERY = "{\n" +
            "  testObject(yourname:\"Phillip\") {\n" +
            "    id\n" +
            "  }\n" +
            "}";
}

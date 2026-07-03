package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.test.targetinput.TargetApi;

public class TargetTest extends ExecutionTestBase {
    @Override
    protected IndexView getIndex() {
        return Indexer.getTestIndex(TargetApi.class);
    }

    @Test
    public void shouldExecuteTargetMethodForInputField() {
        JsonObject response = executeAndGetResult(TARGET_MUTATION, null);
        JsonArray errors = response.getJsonArray("errors");
        JsonObject data = response.getJsonObject("data");

        JsonObject result = data.getJsonObject("save");
        assertEquals("SmallRye", result.getString("title"));
        assertEquals("target:SmallRye:GraphQL", result.getString("value"));
        assertNull(errors);
    }

    @Test
    public void shouldNotExecuteTargetMethodWhenInputFieldIsOmitted() {
        JsonObject response = executeAndGetResult(TARGET_OMITTED_MUTATION, null);
        JsonObject result = response.getJsonObject("data").getJsonObject("save");

        assertEquals("SmallRye", result.getString("title"));
        assertTrue(result.isNull("value"));
        assertNull(response.getJsonArray("errors"));
    }

    @Test
    public void shouldTransformTargetInputFieldValue() {
        JsonObject response = executeAndGetResult(TARGET_NUMBER_MUTATION, null);
        JsonObject result = response.getJsonObject("data").getJsonObject("save");

        assertEquals("SmallRye", result.getString("title"));
        assertEquals("number:42", result.getString("value"));
        assertNull(response.getJsonArray("errors"));
    }

    @Test
    public void shouldInjectContextIntoTargetMethod() {
        JsonObject response = executeAndGetResult(TARGET_CONTEXT_MUTATION, null);
        JsonObject result = response.getJsonObject("data").getJsonObject("save");

        assertEquals("SmallRye", result.getString("title"));
        assertEquals("context:save:GraphQL", result.getString("value"));
        assertNull(response.getJsonArray("errors"));
    }

    private static final String TARGET_MUTATION = "mutation {\n" +
            "  save(input: { title: \"SmallRye\", applyValue: \"GraphQL\" }) {\n" +
            "    title\n" +
            "    value\n" +
            "  }\n" +
            "}";

    private static final String TARGET_OMITTED_MUTATION = "mutation {\n" +
            "  save(input: { title: \"SmallRye\" }) {\n" +
            "    title\n" +
            "    value\n" +
            "  }\n" +
            "}";

    private static final String TARGET_NUMBER_MUTATION = "mutation {\n" +
            "  save(input: { title: \"SmallRye\", applyNumber: 42 }) {\n" +
            "    title\n" +
            "    value\n" +
            "  }\n" +
            "}";

    private static final String TARGET_CONTEXT_MUTATION = "mutation {\n" +
            "  save(input: { title: \"SmallRye\", applyWithContext: \"GraphQL\" }) {\n" +
            "    title\n" +
            "    value\n" +
            "  }\n" +
            "}";
}

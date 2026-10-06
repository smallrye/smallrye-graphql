package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.test.targetinput.TargetApi;
import tools.jackson.databind.node.ObjectNode;

public class TargetTest extends ExecutionTestBase {
    @Override
    protected IndexView getIndex() {
        return Indexer.getTestIndex(TargetApi.class);
    }

    @Test
    public void shouldExecuteTargetMethodForInputField() {
        ObjectNode response = executeAndGetResult(TARGET_MUTATION, null);
        ObjectNode result = (ObjectNode) response.get("data").get("save");
        assertEquals("SmallRye", result.get("title").asText());
        assertEquals("target:SmallRye:GraphQL", result.get("value").asText());
        assertNull(response.get("errors"));
    }

    @Test
    public void shouldNotExecuteTargetMethodWhenInputFieldIsOmitted() {
        ObjectNode response = executeAndGetResult(TARGET_OMITTED_MUTATION, null);
        ObjectNode result = (ObjectNode) response.get("data").get("save");

        assertEquals("SmallRye", result.get("title").asText());
        assertTrue(result.get("value").isNull());
        assertNull(response.get("errors"));
    }

    @Test
    public void shouldTransformTargetInputFieldValue() {
        ObjectNode response = executeAndGetResult(TARGET_NUMBER_MUTATION, null);
        ObjectNode result = (ObjectNode) response.get("data").get("save");

        assertEquals("SmallRye", result.get("title").asText());
        assertEquals("number:42", result.get("value").asText());
        assertNull(response.get("errors"));
    }

    @Test
    public void shouldInjectContextIntoTargetMethod() {
        ObjectNode response = executeAndGetResult(TARGET_CONTEXT_MUTATION, null);
        ObjectNode result = (ObjectNode) response.get("data").get("save");

        assertEquals("SmallRye", result.get("title").asText());
        assertEquals("context:save:GraphQL", result.get("value").asText());
        assertNull(response.get("errors"));
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

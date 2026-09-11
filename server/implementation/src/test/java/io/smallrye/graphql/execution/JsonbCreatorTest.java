package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.*;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.test.jsonbCreator.CreatorApi;
import tools.jackson.databind.node.ObjectNode;

public class JsonbCreatorTest extends ExecutionTestBase {

    protected IndexView getIndex() {
        return Indexer.getTestIndex(CreatorApi.class);
    }

    @Test
    public void testWithValue() {
        ObjectNode data = executeAndGetData("{\n"
                + "  withJsonbCreator(input: {field: \"A\"}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        ObjectNode testObject = (ObjectNode) data.get("withJsonbCreator");
        assertNotNull(testObject);

        String field = testObject.get("field").asText();
        assertEquals("A", field);
    }

    @Test
    public void testWithStaticFactory() {
        ObjectNode data = executeAndGetData("{\n"
                + "  withStaticFactory(input: {field: \"A\"}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        ObjectNode testObject = (ObjectNode) data.get("withStaticFactory");
        assertNotNull(testObject);

        String field = testObject.get("field").asText();
        assertEquals("A", field);
    }

    @Test
    public void testWithMissingValue() {
        ObjectNode data = executeAndGetData("{\n"
                + "  withJsonbCreator(input: {}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        ObjectNode testObject = (ObjectNode) data.get("withJsonbCreator");

        assertNotNull(testObject);
        assertTrue(testObject.get("field").isNull());
    }

    @Test
    public void testWithTransformation() {
        ObjectNode data = executeAndGetData("{\n"
                + "  creatorWithTransformation(input: {field: \"1\"}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        ObjectNode testObject = (ObjectNode) data.get("creatorWithTransformation");
        assertNotNull(testObject);

        String field = testObject.get("field").asText();
        assertEquals("1", field);
    }

    @Test
    public void testWithDefault() {
        ObjectNode data = executeAndGetData("{\n"
                + "  creatorWithFieldDefault(input: {}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        ObjectNode testObject = (ObjectNode) data.get("creatorWithFieldDefault");

        assertNotNull(testObject);

        String field = testObject.get("field").asText();
        assertEquals("Some value", field);
    }

    @Test
    public void testWithParameterDefault() {
        ObjectNode data = executeAndGetData("{\n"
                + "  creatorWithParameterDefault(input: {}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        ObjectNode testObject = (ObjectNode) data.get("creatorWithParameterDefault");
        assertNotNull(testObject);

        String field = testObject.get("field").asText();
        assertEquals("Some value", field);
    }

    @Test
    public void testWithMultipleParameters() {
        ObjectNode data = executeAndGetData("{\n"
                + "  creatorWithMultipleParameters(input: {string: \"Foobar\", localDate:\"2021-01-01\"}) {\n"
                + "    string\n"
                + "    integer\n"
                + "    localDate\n"
                + "  }\n"
                + "}");

        ObjectNode testObject = (ObjectNode) data.get("creatorWithMultipleParameters");
        assertNotNull(testObject);

        String string = testObject.get("string").asText();
        assertEquals("Foobar", string);

        assertTrue(testObject.has("integer") && testObject.get("integer").isNull());

        String localDate = testObject.get("localDate").asText();
        assertEquals("2021-01-01", localDate);
    }
}

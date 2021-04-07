package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.*;

import javax.json.JsonObject;
import javax.json.JsonValue;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.test.jsonbCreator.CreatorApi;

public class JsonbCreatorTest extends ExecutionTestBase {

    protected IndexView getIndex() {
        return Indexer.getTestIndex(CreatorApi.class);
    }

    @Test
    public void testWithValue() {
        JsonObject data = executeAndGetData("{\n"
                + "  withJsonbCreator(input: {field: \"A\"}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        JsonObject testObject = data.getJsonObject("withJsonbCreator");
        assertNotNull(testObject);

        String field = testObject.getJsonString("field").getString();
        assertEquals("A", field);
    }

    @Test
    public void testWithStaticFactory() {
        JsonObject data = executeAndGetData("{\n"
                + "  withStaticFactory(input: {field: \"A\"}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        JsonObject testObject = data.getJsonObject("withStaticFactory");
        assertNotNull(testObject);

        String field = testObject.getJsonString("field").getString();
        assertEquals("A", field);
    }

    @Test
    public void testWithMissingValue() {
        JsonObject data = executeAndGetData("{\n"
                + "  withJsonbCreator(input: {}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        JsonObject testObject = data.getJsonObject("withJsonbCreator");

        assertNotNull(testObject);
        assertEquals(JsonValue.ValueType.NULL, testObject.get("field").getValueType());
    }

    @Test
    public void testWithTransformation() {
        JsonObject data = executeAndGetData("{\n"
                + "  creatorWithTransformation(input: {field: \"1\"}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        JsonObject testObject = data.getJsonObject("creatorWithTransformation");
        assertNotNull(testObject);

        String field = testObject.getJsonString("field").getString();
        assertEquals("1", field);
    }

    @Test
    public void testWithDefault() {
        JsonObject data = executeAndGetData("{\n"
                + "  creatorWithFieldDefault(input: {}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        JsonObject testObject = data.getJsonObject("creatorWithFieldDefault");

        assertNotNull(testObject);

        String field = testObject.getJsonString("field").getString();
        assertEquals("Some value", field);
    }

    @Test
    public void testWithParameterDefault() {
        JsonObject data = executeAndGetData("{\n"
                + "  creatorWithParameterDefault(input: {}) {\n"
                + "    field\n"
                + "  }\n"
                + "}");

        JsonObject testObject = data.getJsonObject("creatorWithParameterDefault");
        assertNotNull(testObject);

        String field = testObject.getJsonString("field").getString();
        assertEquals("Some value", field);
    }

    @Test
    public void testWithMultipleParameters() {
        JsonObject data = executeAndGetData("{\n"
                + "  creatorWithMultipleParameters(input: {string: \"Foobar\", localDate:\"2021-01-01\"}) {\n"
                + "    string\n"
                + "    integer\n"
                + "    localDate\n"
                + "  }\n"
                + "}");

        JsonObject testObject = data.getJsonObject("creatorWithMultipleParameters");
        assertNotNull(testObject);

        String string = testObject.getJsonString("string").getString();
        assertEquals("Foobar", string);

        JsonValue integer = testObject.get("integer");
        assertEquals(JsonValue.ValueType.NULL, integer.getValueType());

        String localDate = testObject.getJsonString("localDate").getString();
        assertEquals("2021-01-01", localDate);
    }
}

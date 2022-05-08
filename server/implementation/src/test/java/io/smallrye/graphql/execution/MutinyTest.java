package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

public class MutinyTest extends ExecutionTestBase {

    protected IndexView getIndex() {
        return Indexer.getTestIndex("io/smallrye/graphql/test/mutiny");
    }

    @Test
    public void testBasicQuery() {
        JsonObject data = executeAndGetData(TEST_QUERY);

        JsonValue jsonValue = data.get("book");
        assertNotNull(jsonValue);

        JsonObject book = jsonValue.asJsonObject();

        assertNotNull(book);

        assertFalse(book.isNull("title"), "title should not be null");
    }

    @Test
    public void testFailureQuery() {
        JsonArray errors = executeAndGetErrors(FAILURE_TEST_QUERY);

        assertNotNull(errors);
        assertEquals(errors.size(), 1);

        String code = errors.get(0).asJsonObject().getJsonObject("extensions").getString("code");

        assertEquals(code, "custom-error", "expected error code: custom-error");
    }

    private static final String TEST_QUERY = "{\n" +
            "  book(name: \"Lord of the Flies\"){\n" +
            "    title\n" +
            "    authors\n" +
            "  }\n" +
            "}";

    private static final String FAILURE_TEST_QUERY = "{\n" +
            "  failedBook(name: \"Nekonomicon\"){\n" +
            "    title\n" +
            "    authors\n" +
            "  }\n" +
            "}";

}

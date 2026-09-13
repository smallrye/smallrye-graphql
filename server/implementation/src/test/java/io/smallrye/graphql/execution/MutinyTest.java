package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.test.mutiny.CustomException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class MutinyTest extends ExecutionTestBase {

    protected IndexView getIndex() {
        return Indexer.getTestIndex("io/smallrye/graphql/test/mutiny");
    }

    @Test
    public void testBasicQuery() {
        ObjectNode data = executeAndGetData(TEST_QUERY);

        JsonNode jsonValue = data.get("book");
        assertNotNull(jsonValue);

        ObjectNode book = (ObjectNode) jsonValue;

        assertNotNull(book);

        assertFalse(book.has("title") && book.get("title").isNull(), "title should not be null");
    }

    @Test
    public void testFailureQuery() {
        ArrayNode errors = executeAndGetErrors(FAILURE_TEST_QUERY);

        assertNotNull(errors);
        assertEquals(errors.size(), 1);
        ObjectNode extensions = (ObjectNode) ((ObjectNode) errors.get(0)).get("extensions");
        assertEquals("custom-error", extensions.get("code").asText(), "error code");
        assertEquals(CustomException.class.getName(), extensions.get("exception").asText(), "exception");
        assertEquals("DataFetchingException", extensions.get("classification").asText(), "classification");
        assertEquals(CustomException.class.getSimpleName().length(), extensions.get("test-extension").asInt(),
                "test extension");
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

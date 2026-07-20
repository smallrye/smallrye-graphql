package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Test a basic async endpoint
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CompletableFutureTest extends ExecutionTestBase {

    protected IndexView getIndex() {
        return Indexer.getTestIndex("io/smallrye/graphql/test/async");
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

    private static final String TEST_QUERY = "{\n" +
            "  book(name: \"Lord of the Flies\"){\n" +
            "    title\n" +
            "    authors\n" +
            "  }\n" +
            "}";

}

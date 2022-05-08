package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.json.JsonObject;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

/**
 * Test a basic grouping
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GroupingTest extends ExecutionTestBase {

    protected IndexView getIndex() {
        return Indexer.getTestIndex("io/smallrye/graphql/test/grouping");
    }

    @Test
    public void testBasicQuery() {
        JsonObject data = executeAndGetData(TEST_QUERY);

        JsonObject books = data.getJsonObject("books");

        assertNotNull(books);

        assertFalse(books.isNull("book"), "book should not be null");
    }

    private static final String TEST_QUERY = "{\n" +
            "  books{\n" +
            "    book(name:\"Lord of the Flies\"){\n" +
            "      title\n" +
            "      authors\n" +
            "      published\n" +
            "    }\n" +
            "  }\n" +
            "}";

}

package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

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
        JsonObject data = executeAndGetData(TEST_QUERY);

        JsonValue jsonValue = data.get("book");
        assertNotNull(jsonValue);

        JsonObject book = jsonValue.asJsonObject();

        assertNotNull(book);

        assertFalse(book.isNull("title"), "title should not be null");
    }

    private static final String TEST_QUERY = "{\n" +
            "  book(name: \"Lord of the Flies\"){\n" +
            "    title\n" +
            "    authors\n" +
            "  }\n" +
            "}";

}

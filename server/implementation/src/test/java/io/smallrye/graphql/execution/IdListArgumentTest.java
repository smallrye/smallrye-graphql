package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

/**
 * Reproducer for https://github.com/smallrye/smallrye-graphql/issues/2733: a {@code @Id List<String>} query
 * argument used to print correctly as {@code [ID]} in the schema but fail execution with
 * "is not a valid 'Unknown Scalar Type [java.util.List]'".
 */
public class IdListArgumentTest extends ExecutionTestBase {

    @Override
    protected IndexView getIndex() {
        return Indexer.getTestIndex("io/smallrye/graphql/test/id");
    }

    @Test
    public void listOfIdsAsLiteral() {
        JsonObject data = executeAndGetData("{ echoIds(ids: [\"a\", \"b\"]) }");
        assertNull(executeAndGetErrors("{ echoIds(ids: [\"a\", \"b\"]) }"));

        JsonArray echoIds = data.getJsonArray("echoIds");
        assertEquals(2, echoIds.size());
        assertEquals("a", echoIds.getString(0));
        assertEquals("b", echoIds.getString(1));
    }

    @Test
    public void singleIdStillWorks() {
        JsonObject data = executeAndGetData("{ echoId(id: \"a\") }");
        assertEquals("a", data.getString("echoId"));
    }
}

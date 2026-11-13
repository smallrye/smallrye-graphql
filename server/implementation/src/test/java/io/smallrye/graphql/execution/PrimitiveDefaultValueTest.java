package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.api.Test;

/**
 * Reproducer for https://github.com/smallrye/smallrye-graphql/issues/1565: a primitive argument with a
 * {@code @DefaultValue} is nullable in the schema, so an explicit {@code null} used to fail with an
 * IllegalArgumentException when invoking the method.
 */
public class PrimitiveDefaultValueTest extends ExecutionTestBase {

    @Override
    protected IndexView getIndex() {
        return Indexer.getTestIndex("io/smallrye/graphql/test/primitivedefault");
    }

    @Test
    public void absentPrimitiveArgumentUsesDefault() {
        assertEquals(5, executeAndGetData("{ intDefault }").getInt("intDefault"));
    }

    @Test
    public void explicitNullPrimitiveArgumentUsesDefault() {
        String query = "{ intDefault(i: null) }";
        assertNull(executeAndGetErrors(query));
        assertEquals(5, executeAndGetData(query).getInt("intDefault"));
    }
}

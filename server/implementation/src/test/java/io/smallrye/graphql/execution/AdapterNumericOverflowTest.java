package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.json.JsonObject;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AdapterNumericOverflowTest extends ExecutionTestBase {

    @Override
    protected IndexView getIndex() {
        return Indexer.getTestIndex("io/smallrye/graphql/test/overflowadapter");
    }

    @ParameterizedTest
    @ValueSource(strings = { "-9223372036854775808", "9223372036854775807" })
    void adaptedLongAcceptsInclusiveBounds(String value) {
        JsonObject result = executeAndGetResult("{ overflowAdaptedLong(input: \"" + value + "\") }", null);
        assertNull(result.get("errors"), result::toString);
        assertEquals(Long.parseLong(value), result.getJsonObject("data").getJsonNumber("overflowAdaptedLong").longValueExact());
    }

    @ParameterizedTest
    @ValueSource(strings = { "-99999999999999999999999999999", "99999999999999999999999999999" })
    void adaptedLongRejectsOverflowInsteadOfWrapping(String value) {
        JsonObject result = executeAndGetResult("{ overflowAdaptedLong(input: \"" + value + "\") }", null);
        assertNotNull(result.getJsonArray("errors"), result::toString);
        assertEquals("ValidationError", result.getJsonArray("errors").getJsonObject(0)
                .getJsonObject("extensions").getString("classification"));
        JsonObject data = result.getJsonObject("data");
        assertTrue(data == null || data.isNull("overflowAdaptedLong"), result::toString);
    }
}

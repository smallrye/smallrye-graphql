package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import org.jboss.jandex.IndexView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class NumericOverflowTest extends ExecutionTestBase {

    @Override
    protected IndexView getIndex() {
        return Indexer.getTestIndex("io/smallrye/graphql/test/overflow");
    }

    static Stream<Arguments> invalidFields() {
        return Stream.of("overflowPojo", "overflowRecord").flatMap(query -> Stream.of(
                Arguments.of(query, "primitiveInt", "2147483648"),
                Arguments.of(query, "boxedInt", "-2147483649"),
                Arguments.of(query, "primitiveLong", "99999999999999999999999999999"),
                Arguments.of(query, "boxedLong", "-99999999999999999999999999999"),
                Arguments.of(query, "primitiveInt", "1.5"),
                Arguments.of(query, "boxedLong", "1.5")));
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void rejectsOverflowingLiteralBeforeCallingApplication(String query, String field, String value) {
        String input = "{primitiveInt: 42, boxedInt: 42, primitiveLong: 42, boxedLong: 42}"
                .replace(field + ": 42", field + ": " + (field.endsWith("Long") ? "\"" + value + "\"" : value));
        JsonObject result = executeAndGetResult("{ " + query + "(input: " + input + ") }", null);
        assertRejected(result, query);
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void rejectsOverflowingVariableBeforeCallingApplication(String query, String field, String value) {
        String inputType = query.equals("overflowPojo") ? "NumberPojoInput" : "NumberRecordInput";
        JsonObjectBuilder input = Json.createObjectBuilder();
        for (String name : new String[] { "primitiveInt", "boxedInt", "primitiveLong", "boxedLong" }) {
            input.add(name, new BigDecimal(name.equals(field) ? value : "42"));
        }
        JsonObject request = Json.createObjectBuilder()
                .add("query", "query Probe($input: " + inputType + "!) { " + query + "(input: $input) }")
                .add("variables", Json.createObjectBuilder().add("input", input)).build();
        JsonObjectResponseWriter writer = new JsonObjectResponseWriter(request);
        executionService.executeSync(request, writer);
        assertRejected(writer.getOutput(), query);
    }

    @ParameterizedTest
    @ValueSource(strings = { "overflowPojo", "overflowRecord" })
    void acceptsExactInclusiveBounds(String query) {
        JsonObject result = executeAndGetResult("{ " + query + "(input: {primitiveInt: -2147483648, "
                + "boxedInt: 2147483647, primitiveLong: \"9223372036854775807\", "
                + "boxedLong: \"-9223372036854775808\"}) }", null);
        assertNull(result.get("errors"), result::toString);
        assertEquals("-2147483648:2147483647:9223372036854775807:-9223372036854775808",
                result.getJsonObject("data").getString(query));
    }

    private static void assertRejected(JsonObject result, String query) {
        assertNotNull(result.getJsonArray("errors"), result::toString);
        assertFalse(result.getJsonArray("errors").isEmpty(), result::toString);
        JsonObject data = result.getJsonObject("data");
        assertTrue(data == null || !data.containsKey(query) || data.isNull(query), result::toString);
    }
}

package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class OptionalBindingTest extends ExecutionTestBase {

    @Override
    protected IndexView getIndex() {
        try {
            return Index.of(OptionalApi.class, OptionalInput.class, OptionalRecord.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static Stream<Arguments> optionalArguments() {
        return Stream.of(
                new String[] { "optionalGeneric", "Int", "Optional.empty", "Optional[7]" },
                new String[] { "optionalInt", "Int", "OptionalInt.empty", "OptionalInt[7]" },
                new String[] { "optionalLong", "BigInteger", "OptionalLong.empty", "OptionalLong[7]" },
                new String[] { "optionalDouble", "Float", "OptionalDouble.empty", "OptionalDouble[7.0]" })
                .flatMap(probe -> {
                    String query = probe[0];
                    String variableQuery = "query($v:" + probe[1] + "){result:" + query + "(value:$v)}";
                    String defaultQuery = "query($v:" + probe[1] + "=7){result:" + query + "(value:$v)}";
                    JsonObject emptyVariables = Json.createObjectBuilder().build();
                    JsonObject nullVariable = Json.createObjectBuilder().addNull("v").build();
                    return Stream.of(
                            Arguments.of("{result:" + query + "}", emptyVariables, probe[2]),
                            Arguments.of("{result:" + query + "(value:null)}", emptyVariables, probe[2]),
                            Arguments.of("{result:" + query + "(value:7)}", emptyVariables, probe[3]),
                            Arguments.of(variableQuery, emptyVariables, probe[2]),
                            Arguments.of(variableQuery, nullVariable, probe[2]),
                            Arguments.of(variableQuery, Json.createObjectBuilder().add("v", 7).build(), probe[3]),
                            Arguments.of(defaultQuery, emptyVariables, probe[3]),
                            Arguments.of(defaultQuery, nullVariable, probe[2]));
                });
    }

    @ParameterizedTest
    @MethodSource("optionalArguments")
    void optionalArgumentsUseEmptyForBothOmittedAndNullValues(String query, JsonObject variables, String expected) {
        assertResult(query, variables, expected);
    }

    static Stream<Arguments> optionalInputFields() {
        String empty = "Optional.empty/OptionalInt.empty/OptionalLong.empty/OptionalDouble.empty";
        String present = "Optional[7]/OptionalInt[7]/OptionalLong[7]/OptionalDouble[7.0]";
        return Stream.of("optionalPojo", "optionalRecord").flatMap(query -> Stream.of(
                new String[] { "{}", query.equals("optionalPojo") ? "null/null/null/null" : empty },
                new String[] { "{\"generic\":null,\"intValue\":null,\"longValue\":null,\"doubleValue\":null}", empty },
                new String[] { "{\"generic\":7,\"intValue\":7,\"longValue\":7,\"doubleValue\":7}", present })
                .flatMap(probe -> {
                    JsonObject input;
                    try (JsonReader reader = Json.createReader(new StringReader(probe[0]))) {
                        input = reader.readObject();
                    }
                    String literal = probe[0].replace("\"", "");
                    String inputType = query.equals("optionalPojo") ? "OptionalInputInput" : "OptionalRecordInput";
                    return Stream.of(
                            Arguments.of("{result:" + query + "(value:" + literal + ")}", JsonObject.EMPTY_JSON_OBJECT,
                                    probe[1]),
                            Arguments.of("query($v:" + inputType + "){result:" + query + "(value:$v)}",
                                    Json.createObjectBuilder().add("v", input).build(), probe[1]));
                }));
    }

    @ParameterizedTest
    @MethodSource("optionalInputFields")
    void omittedPojoFieldsKeepTheirInitialValueWhileRecordComponentsUseEmpty(String query, JsonObject variables,
            String expected) {
        assertResult(query, variables, expected);
    }

    private void assertResult(String query, JsonObject variables, String expected) {
        JsonObject request = Json.createObjectBuilder().add("query", query).add("variables", variables).build();
        JsonObjectResponseWriter writer = new JsonObjectResponseWriter(request);
        executionService.executeSync(request, writer);
        JsonObject result = writer.getOutput();
        assertNull(result.get("errors"), result::toString);
        assertEquals(expected, result.getJsonObject("data").getString("result"));
    }

    @GraphQLApi
    public static class OptionalApi {
        @Query
        public String optionalGeneric(@Name("value") Optional<Integer> value) {
            return String.valueOf(value);
        }

        @Query
        public String optionalInt(@Name("value") OptionalInt value) {
            return String.valueOf(value);
        }

        @Query
        public String optionalLong(@Name("value") OptionalLong value) {
            return String.valueOf(value);
        }

        @Query
        public String optionalDouble(@Name("value") OptionalDouble value) {
            return String.valueOf(value);
        }

        @Query
        public String optionalPojo(@Name("value") OptionalInput value) {
            return value.generic + "/" + value.intValue + "/" + value.longValue + "/" + value.doubleValue;
        }

        @Query
        public String optionalRecord(@Name("value") OptionalRecord value) {
            return value.generic() + "/" + value.intValue() + "/" + value.longValue() + "/" + value.doubleValue();
        }
    }

    public static class OptionalInput {
        public Optional<Integer> generic;
        public OptionalInt intValue;
        public OptionalLong longValue;
        public OptionalDouble doubleValue;
    }

    public record OptionalRecord(Optional<Integer> generic, OptionalInt intValue, OptionalLong longValue,
            OptionalDouble doubleValue) {
    }
}

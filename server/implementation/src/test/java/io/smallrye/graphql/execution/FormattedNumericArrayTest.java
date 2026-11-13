package io.smallrye.graphql.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import jakarta.json.JsonObject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.NumberFormat;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.jandex.Index;
import org.jboss.jandex.IndexView;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FormattedNumericArrayTest extends ExecutionTestBase {

    @Override
    protected IndexView getIndex() {
        try {
            return Index.of(FormattedArrayApi.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    static Stream<Arguments> formattedArrays() {
        return Stream.of(
                Arguments.of("formattedBoxedDoubles", "[\"1,234.50\",\"2.00\"]"),
                Arguments.of("formattedPrimitiveInts", "[\"1,234.00\",\"2.00\"]"),
                Arguments.of("formattedArrayInList", "[[\"1,234.50\",\"2.00\"]]"),
                Arguments.of("formattedNullableDoubles", "[\"1,234.50\",null,\"2.00\"]"));
    }

    @ParameterizedTest
    @MethodSource("formattedArrays")
    void numericArrayElementsKeepTheirNumberFormat(String query, String expected) {
        JsonObject result = executeAndGetResult("{ result: " + query + " }", null);
        assertNull(result.get("errors"), result::toString);
        assertEquals(expected, result.getJsonObject("data").get("result").toString());
    }

    @GraphQLApi
    public static class FormattedArrayApi {
        @Query
        @NumberFormat(value = "#,##0.00", locale = "en-US")
        public Double[] formattedBoxedDoubles() {
            return new Double[] { 1234.5, 2.0 };
        }

        @Query
        @NumberFormat(value = "#,##0.00", locale = "en-US")
        public int[] formattedPrimitiveInts() {
            return new int[] { 1234, 2 };
        }

        @Query
        @NumberFormat(value = "#,##0.00", locale = "en-US")
        public List<Double[]> formattedArrayInList() {
            return List.<Double[]> of(new Double[] { 1234.5, 2.0 });
        }

        @Query
        @NumberFormat(value = "#,##0.00", locale = "en-US")
        public Double[] formattedNullableDoubles() {
            return new Double[] { 1234.5, null, 2.0 };
        }
    }
}

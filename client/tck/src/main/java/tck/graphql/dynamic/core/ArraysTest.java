package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.OperationType.QUERY;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Document;
import tck.graphql.dynamic.helper.AssertGraphQL;
import tck.graphql.dynamic.helper.Utils;

public class ArraysTest {

    @Test
    public void arraysTest() throws IOException, URISyntaxException {
        String expectedRequest = Utils.getResourceFileContent("core/arrays.graphql");

        Document document = document(
                operation(QUERY, "arrayHolderQuery",
                        field("arrayHolder",
                                args(
                                        arg("arrayHolder", inputObject(
                                                prop("boolPrimitiveArray", new boolean[] { true, false, true }),
                                                prop("boolObjectArray", new Boolean[] { true, false, true }),

                                                prop("bytePrimitiveArray", new byte[] { 0, 2, 3 }),
                                                prop("byteObjectArray", new Byte[] { 0, 2, 3 }),

                                                prop("shortPrimitiveArray", new short[] { 78, 789, 645 }),
                                                prop("shortObjectArray", new Short[] { 78, 789, 645 }),

                                                prop("intPrimitiveArray", new int[] { 78, 65, 12354 }),
                                                prop("intObjectArray", new Integer[] { 78, 65, 12354 }),

                                                prop("longPrimitiveArray", new long[] { 789L, 947894L, 1874448L }),
                                                prop("longObjectArray", new Long[] { 789L, 947894L, 1874448L }),

                                                prop("floatPrimitiveArray", new float[] { 1567.654f, 8765f, 123789456.1851f }),
                                                prop("floatObjectArray", new Float[] { 1567.654f, 8765f, 123789456.1851f }),

                                                prop("doublePrimitiveArray",
                                                        new double[] { 789.3242d, 1815d, 98765421.654897d }),
                                                prop("doubleObjectArray", new Double[] { 789.3242d, 1815d, 98765421.654897d }),

                                                prop("bigIntegerArray",
                                                        new BigInteger[] { BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN }),
                                                prop("bigDecimalArray",
                                                        new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TEN }),

                                                prop("charPrimitiveArray", new char[] { 'f', 'o', 'o' }),
                                                prop("charObjectArray", new Character[] { 'f', 'o', 'o' }),

                                                prop("stringArray", new String[] { "foo", "bar", "baz" }),

                                                prop("uuidArray", new UUID[]{UUID.fromString("fc4bb4f4-13fe-4908-8d6a-afa64f1b56c9"),
                                                    UUID.fromString("863c9e3c-7538-41b9-9d63-0852f6a50815")})
                                        ))),
                                field("boolPrimitiveArray"),
                                field("boolObjectArray"),

                                field("bytePrimitiveArray"),
                                field("byteObjectArray"),

                                field("shortPrimitiveArray"),
                                field("shortObjectArray"),

                                field("intPrimitiveArray"),
                                field("intObjectArray"),

                                field("longPrimitiveArray"),
                                field("longObjectArray"),

                                field("floatPrimitiveArray"),
                                field("floatObjectArray"),

                                field("doublePrimitiveArray"),
                                field("doubleObjectArray"),

                                field("bigIntegerArray"),
                                field("bigDecimalArray"),

                                field("charPrimitiveArray"),
                                field("charObjectArray"),

                                field("stringArray"),

                                field("uuidArray")
                        )));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }
}

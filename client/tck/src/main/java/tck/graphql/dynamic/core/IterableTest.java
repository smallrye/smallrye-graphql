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
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Document;
import tck.graphql.dynamic.helper.AssertGraphQL;
import tck.graphql.dynamic.helper.Utils;

public class IterableTest {

    @Test
    public void iterableTest() throws IOException, URISyntaxException {
        String expectedRequest = Utils.getResourceFileContent("core/iterable.graphql");

        Document document = document(
                operation(QUERY, "iterableHolderQuery",
                        field("iterableHolder",
                                args(
                                        arg("iterableHolder", inputObject(

                                                prop("boolObjectList", List.of(true, false)),

                                                prop("byteObjectList", List.of((byte) 0, (byte) 2, (byte) 3)),

                                                prop("shortObjectList", List.of((short) 78, (short) 789, (short) 645)),

                                                prop("intObjectList", List.of(78, 65, 12354)),

                                                prop("longObjectList", List.of(789L, 947894L, 1874448L)),

                                                prop("floatObjectList", List.of(1567.1f, 8765f, 3.14159f)),

                                                prop("doubleObjectList", List.of(789.1d, 1815d, 3.14159d)),

                                                prop("bigIntegerList",
                                                        List.of(BigInteger.ZERO, BigInteger.ONE, BigInteger.TEN)),

                                                prop("bigDecimalList",
                                                        List.of(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.TEN)),

                                                prop("charObjectList", List.of('f', 'o', 'o')),

                                                prop("stringList", List.of("foo", "bar", "baz")),

                                                prop("uuidList",
                                                        List.of(UUID.fromString("fc4bb4f4-13fe-4908-8d6a-afa64f1b56c9"),
                                                                UUID.fromString("863c9e3c-7538-41b9-9d63-0852f6a50815")))

                                        ))),
                                field("boolObjectList"),

                                field("byteObjectList"),

                                field("shortObjectList"),

                                field("intObjectList"),

                                field("longObjectList"),

                                field("floatObjectList"),

                                field("doubleObjectList"),

                                field("bigIntegerList"),

                                field("bigDecimalList"),

                                field("charObjectList"),

                                field("stringList"),

                                field("uuidList"))));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }
}

package org.eclipse.microprofile.graphql.client.tck.core;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.OperationType.MUTATION;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;

import org.eclipse.microprofile.graphql.client.tck.helper.AssertGraphQL;
import org.eclipse.microprofile.graphql.client.tck.helper.Utils;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Document;

public class ScalarsTest {

    @Test
    public void scalarsTest() throws IOException, URISyntaxException {
        String expectedRequest = Utils.getResourceFileContent("core/scalars.graphql");

        Document document = document(
                operation(MUTATION, "scalarHolderMutation",
                        field("scalarHolder",
                                args(
                                        arg("scalarHolder", inputObject(
                                                prop("booleanPrimitive", false),
                                                prop("booleanObject", Boolean.valueOf(true)),

                                                prop("bytePrimitive", Byte.MIN_VALUE),
                                                prop("byteObject", Byte.valueOf(Byte.MAX_VALUE)),

                                                prop("shortPrimitive", Short.MIN_VALUE),
                                                prop("shortObject", Short.valueOf(Short.MAX_VALUE)),

                                                prop("intPrimitive", Integer.MIN_VALUE + 1),
                                                prop("intObject", Integer.valueOf(Integer.MAX_VALUE)),

                                                prop("longPrimitive", Long.MIN_VALUE),
                                                prop("longObject", Long.valueOf(Long.MAX_VALUE)),

                                                prop("floatPrimitive", Float.MIN_VALUE),
                                                prop("floatObject", Float.valueOf(Float.MAX_VALUE)),

                                                prop("doublePrimitive", Double.MIN_VALUE),
                                                prop("doubleObject", Double.valueOf(Double.MAX_VALUE)),

                                                prop("bigInteger", BigInteger.TEN),
                                                prop("bigDecimal", BigDecimal.TEN),

                                                prop("charPrimitive", 'a'),
                                                prop("charObject", Character.valueOf('Z')),

                                                prop("stringObject", "Hello World !")))),
                                field("booleanPrimitive"),
                                field("booleanObject"),

                                field("bytePrimitive"),
                                field("byteObject"),

                                field("shortPrimitive"),
                                field("shortObject"),

                                field("intPrimitive"),
                                field("intObject"),

                                field("longPrimitive"),
                                field("longObject"),

                                field("floatPrimitive"),
                                field("floatObject"),

                                field("doublePrimitive"),
                                field("doubleObject"),

                                field("bigInteger"),
                                field("bigDecimal"),

                                field("charPrimitive"),
                                field("charObject"),

                                field("stringObject"))));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }
}

package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.InputObject.inputObject;
import static io.smallrye.graphql.client.core.InputObjectField.prop;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.OperationType.MUTATION;
import static io.smallrye.graphql.client.core.OperationType.QUERY;
import static io.smallrye.graphql.client.core.ScalarType.GQL_BOOL;
import static io.smallrye.graphql.client.core.ScalarType.GQL_FLOAT;
import static io.smallrye.graphql.client.core.ScalarType.GQL_ID;
import static io.smallrye.graphql.client.core.ScalarType.GQL_INT;
import static io.smallrye.graphql.client.core.ScalarType.GQL_STRING;
import static io.smallrye.graphql.client.core.Variable.var;
import static io.smallrye.graphql.client.core.Variable.vars;
import static io.smallrye.graphql.client.core.VariableType.list;
import static io.smallrye.graphql.client.core.VariableType.nonNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.core.ScalarType;
import io.smallrye.graphql.client.core.Variable;
import tck.graphql.dynamic.helper.AssertGraphQL;
import tck.graphql.dynamic.helper.Utils;

public class VariablesTest {

    @Test
    public void variablesDefaultValueTest() throws IOException, URISyntaxException {
        String expectedRequest = Utils.getResourceFileContent("core/variablesDefaultValue.graphql");

        Variable varName = var("name", GQL_STRING, "Lee Byron");

        Document document = document(
                operation(QUERY,
                        vars(varName),
                        field("helloYou", arg("name", varName))));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    @Test
    public void variablesFlatTest() throws IOException, URISyntaxException {
        String expectedRequest = Utils.getResourceFileContent("core/variablesFlat.graphql");

        Variable varBool = var("varBool", nonNull(GQL_BOOL));
        Variable varDouble = var("varDouble", nonNull(GQL_FLOAT));
        Variable varString = var("varString", nonNull(GQL_STRING));

        Document document = document(
                operation(QUERY,
                        vars(
                                varBool,
                                varDouble,
                                varString),
                        field("withArgWithSubField", args(
                                arg("aString", varString),
                                arg("aDouble", varDouble),
                                arg("aBool", varBool)),
                                field("bool"),
                                field("double"),
                                field("string"))));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    @Test
    public void variablesInInputObjectTest() throws IOException, URISyntaxException {
        String expectedRequest = Utils.getResourceFileContent("core/variablesInInputObject.graphql");

        Variable varBool = var("varBool", nonNull(GQL_BOOL));
        Variable varInt = var("varInt", nonNull(GQL_INT));
        Variable varFloat = var("varFloat", nonNull(GQL_FLOAT));
        Variable varString = var("varString", nonNull(GQL_STRING));
        Variable varID = var("varID", GQL_ID);

        Document document = document(
                operation(QUERY,
                        vars(
                                varBool,
                                varInt,
                                varFloat,
                                varString,
                                varID),
                        field("basicScalarHolder", args(
                                arg("basicScalarHolder", inputObject(
                                        prop("bool", varBool),
                                        prop("int", varInt),
                                        prop("float", varFloat),
                                        prop("string", varString),
                                        prop("iD", varID)))),
                                field("bool"),
                                field("int"),
                                field("float"),
                                field("string"),
                                field("iD"))));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    @Test
    public void variablesArraysTest() throws IOException, URISyntaxException {
        String expectedRequest = Utils.getResourceFileContent("core/variablesArrays.graphql");

        Variable varInt1 = var("varInt_1", list(GQL_INT));
        Variable varInt1bang = var("varInt_1_bang", nonNull(list(GQL_INT)));
        Variable varIntbang1 = var("varInt_bang_1", list(nonNull(GQL_INT)));
        Variable varInt12 = var("varInt_1_2", list(list(GQL_INT)));
        Variable varInt123 = var("varInt_1_2_3", list(list(list(GQL_INT))));
        Variable varInt1bang23bang = var("varInt_1_bang_2_3_bang",
                nonNull(list(
                        list(
                                nonNull(list(GQL_INT))))));
        Variable varIntbang1bang2bang3bang = var("varInt_bang_1_bang_2_bang_3_bang",
                nonNull(list(
                        nonNull(list(
                                nonNull(list(
                                        nonNull(GQL_INT))))))));

        /*
         * To use for e2e tests
         * {
         * "varInt_1": [12, 34, 567, 89],
         * "varInt_1_2": null,
         * "varInt_1_2_3": [[[1, null], [3, 4]], null, [[7], null]],
         * "varInt_1_bang": [null],
         * "varInt_bang_1": null,
         * "varInt_1_bang_2_3_bang": [[[null, 2], [null, 4]], [[5, 6]], null],
         * "varInt_bang_1_bang_2_bang_3_bang": [[[1, 2], [3, 4]], [[5, 6], [7], [8, 9]]]
         * }
         */

        Document document = document(
                operation(MUTATION,
                        vars(
                                varInt1,
                                varInt12,
                                varInt123,
                                varInt1bang,
                                varIntbang1,
                                varInt1bang23bang,
                                varIntbang1bang2bang3bang),
                        field("nestedArraysHolder", args(
                                arg("nestedArraysHolder", inputObject(
                                        prop("int_1", varInt1),
                                        prop("int_1_2", varInt12),
                                        prop("int_1_2_3", varInt123),
                                        prop("int_1_bang", varInt1bang),
                                        prop("int_bang_1", varIntbang1),
                                        prop("int_1_bang_2_3_bang", varInt1bang23bang),
                                        prop("int_bang_1_bang_2_bang_3_bang", varIntbang1bang2bang3bang)))),
                                field("int_1"),
                                field("int_1_2"),
                                field("int_1_2_3"),
                                field("int_1_bang"),
                                field("int_1_bang_2_3_bang"),
                                field("int_bang_1"),
                                field("int_bang_1_bang_2_bang_3_bang"))));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    @Test
    public void variablesShouldNotThrowExceptionForValidNameTest() {
        ScalarType scalarType = GQL_INT;
        assertDoesNotThrow(() -> var("myVar", scalarType));
        assertDoesNotThrow(() -> var("_myVar", scalarType));
        assertDoesNotThrow(() -> var("my_var", scalarType));
        assertDoesNotThrow(() -> var("my123Var", scalarType));
        assertDoesNotThrow(() -> var("v", scalarType));
        assertDoesNotThrow(() -> var("_", scalarType));
        assertDoesNotThrow(() -> var("va_r", scalarType));
    }

    @Test
    public void variablesShouldThrowExceptionForInvalidNameTest() {
        ScalarType scalarType = GQL_INT;
        assertThrows(IllegalArgumentException.class, () -> var("123", scalarType));
        assertThrows(IllegalArgumentException.class, () -> var("my_var$", scalarType));
        assertThrows(IllegalArgumentException.class, () -> var("va:r", scalarType));
        assertThrows(IllegalArgumentException.class, () -> var("", scalarType));
        assertThrows(IllegalArgumentException.class, () -> var(":var", scalarType));
        assertThrows(IllegalArgumentException.class, () -> var("va:r:", scalarType));
        assertThrows(IllegalArgumentException.class, () -> var(null, scalarType));
    }

}

package tck.graphql.dynamic.core;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Directive.directive;
import static io.smallrye.graphql.client.core.Directive.directives;
import static io.smallrye.graphql.client.core.DirectiveArgument.directiveArg;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Field.fieldWithDirectives;
import static io.smallrye.graphql.client.core.InlineFragment.on;
import static io.smallrye.graphql.client.core.Operation.operation;
import static io.smallrye.graphql.client.core.Operation.operationWithDirectives;
import static io.smallrye.graphql.client.core.OperationType.MUTATION;
import static io.smallrye.graphql.client.core.OperationType.QUERY;
import static io.smallrye.graphql.client.core.OperationType.SUBSCRIPTION;
import static io.smallrye.graphql.client.core.ScalarType.GQL_ID;
import static io.smallrye.graphql.client.core.ScalarType.GQL_INT;
import static io.smallrye.graphql.client.core.Variable.var;
import static io.smallrye.graphql.client.core.Variable.varWithDirectives;
import static io.smallrye.graphql.client.core.Variable.vars;
import static io.smallrye.graphql.client.core.VariableType.nonNull;
import static io.smallrye.graphql.client.core.VariableType.varType;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.core.Variable;
import tck.graphql.dynamic.helper.AssertGraphQL;
import tck.graphql.dynamic.helper.Utils;

/**
 * This class tests creating queries (via DSL) that contain user-made client-side directives (with/without arguments).
 */
public class BuildDirectiveTest {
    /*--------TESTING INLINE FRAGMENTS--------*/
    @Test
    public void buildInlineFragmentWithDirectiveTest() {
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesInlineFragment.graphql");
        Document document = document(
                operation(QUERY,
                        "SimpleQuery",
                        field("customers",
                                field("name"),
                                on("Customer",
                                        directives(
                                                directive("myDirective",
                                                        directiveArg("flag", true))),
                                        field("orders",
                                                field("id"))))));

        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    /*--------TESTING OPERATION--------*/

    // (directives, fields)
    @Test
    public void buildOperationWithDirectivesTest() {
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesOperationNoNameNoVars.graphql");
        Document document = document(
                operationWithDirectives(
                        directives(directive("myDirective")),
                        field("customers",
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (variables, directives, fields)
    @Test
    public void buildOperationWithVariablesAndDirectivesTest() {
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesOperationNoName.graphql");
        Variable variable = var("var1", GQL_INT);
        Document document = document(
                operationWithDirectives(
                        vars(var("var1", "Int")),
                        directives(directive("myDirective")),
                        field("customerById",
                                args(arg("id", variable)),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (OperationType, variables, directives, fields)
    @Test
    public void buildOperationWithOperationTypeAndVariablesAndDirectivesTest() {
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesOperationNoName.graphql")
                .replaceFirst("query", "mutation");
        Variable variable = var("var1", GQL_INT);
        Document document = document(
                operationWithDirectives(MUTATION,
                        vars(variable),
                        directives(directive("myDirective")),
                        field("customerById",
                                args(arg("id", variable)),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (name, directives, fields)
    @Test
    public void buildOperationWithNameAndDirectivesTest() {
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesOperationNoVars.graphql");
        Document document = document(
                operationWithDirectives("SimpleQuery",
                        directives(
                                directive(
                                        "myDirective",
                                        directiveArg("directiveArg", 3.1415926535))),
                        field("customers",
                                field("id"),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (name, variables, directives, fields)
    @Test
    public void buildOperationWithNameAndVariablesAndDirectivesTest() {
        Variable variable = var("var1", GQL_ID);
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesOperationWithEverything.graphql");
        Document document = document(
                operationWithDirectives("SimpleQuery",
                        vars(variable),
                        directives(directive("deprecated", directiveArg("reason",
                                "old version")),
                                directive("myDirective")),
                        field("customerById", args(arg("id", variable)),
                                field("id"),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (operationType, name, variables, directives, fields)
    @Test
    public void buildOperationWithEverythingTest() {
        Variable variable = var("var1", GQL_ID);
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesOperationWithEverything.graphql")
                .replaceFirst("query", "subscription");
        Document document = document(
                operationWithDirectives(SUBSCRIPTION,
                        "SimpleQuery",
                        vars(variable),
                        directives(
                                directive("deprecated", directiveArg("reason",
                                        "old version")),
                                directive("myDirective")),
                        field("customerById", args(arg("id", variable)),
                                field("id"),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    /*--------TESTING VARIABLES--------*/
    // (name, scalarType, directives)
    @Test
    public void buildVariableWithScalarTypeAndDirectiveTest() {
        Variable variable = varWithDirectives("arg1", GQL_INT,
                directives(directive("myDirective")));
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesVariableWithScalarType.graphql");
        Document document = document(
                operation(
                        "SimpleQuery",
                        vars(variable),
                        field("customerById", args(arg("id", variable)),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (name, scalarType, defaultValue, directives)
    @Test
    public void buildVariableWithScalarTypeAndDefaultValueAndDirectivesTest() {
        Variable variable = varWithDirectives("arg1", GQL_INT, 3,
                directives(directive("myDirective")));
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesVariableWIthScalarTypeDefaultValue.graphql");

        Document document = document(
                operation(
                        "SimpleQuery",
                        vars(variable),
                        field("customerById", args(arg("id", variable)),
                                field("id"),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);

    }

    // (name, objectTypeName, directives)
    @Test
    public void buildVariableWithObjectTypeNameAndDirectivesTest() {
        Variable variable = varWithDirectives("arg1", "VARCHAR2",
                directives(directive("myDirective")));
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesVariableWithObjectTypeName.graphql");
        Document document = document(
                operation(
                        "SimpleQuery",
                        vars(variable),
                        field("customerByName",
                                args(arg("name", variable)),
                                field("id"),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (name, objectTypeName, defaultValue, directives)
    @Test
    public void buildVariableWithObjectTypeNameAndDefaultValueAndDirectivesTest() {
        Variable variable = varWithDirectives("arg1", "VARCHAR", "default",
                directives(directive("myDirective")));
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesVariableWithObjectTypeNameDefaultValue.graphql");
        Document document = document(
                operation(
                        "SimpleQuery",
                        vars(variable),
                        field("customerByName",
                                args(arg("name", variable)),
                                field("id"),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (name, variableType, directives)
    @Test
    public void buildVariableWithVariableTypeAndDirectivesTest() {
        Variable variable1 = varWithDirectives("arg1", varType(GQL_ID), directives(directive("myDirective1")));
        Variable variable2 = varWithDirectives("arg2", varType("VARCHAR2"), directives(directive("myDirective2",
                directiveArg("reason", "something")),
                directive("myDirective3")));

        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesVariableWithVariableType.graphql");
        Document document = document(
                operation(
                        "SomeQuery",
                        vars(variable1, variable2),
                        field("customerByIdAndName",
                                args(arg("id", variable1), arg("name", variable2)),
                                field("id"),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (name, variableType, defaultValue, directives)
    @Test
    public void buildVariableWithVariableTypeAndDefaultValueAndDirectivesTest() {
        Variable variable = varWithDirectives(
                "arg1",
                nonNull(varType(GQL_ID)),
                1,
                directives(directive("myDirective")));

        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesVariableWithVariableTypeDefaultValue.graphql");
        Document document = document(
                operation(
                        "AnotherQuery",
                        vars(variable),
                        field("customerById",
                                args(arg("id", variable)),
                                field("id"),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    /*--------TESTING FIELDS--------*/

    // (directives)
    @Test
    public void buildFieldWithDirectivesTest() {
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesField.graphql");
        Document document = document(
                operation(
                        "SimpleQuery",
                        field("customers",
                                fieldWithDirectives("id", directive("myDirective")),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (arguments, directives)
    @Test
    public void buildFieldWithArgumentsAndDirectivesTest() {
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesFieldWithArguments.graphql");
        Document document = document(
                operation(
                        "SimpleQuery",
                        field("customers",
                                fieldWithDirectives("id",
                                        args(arg("id", 3)),
                                        directives(directive("myDirective"))),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (directives, subFields)
    @Test
    public void buildFieldWithSubFieldsAndDirectivesTest() {
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesFieldWithSubFields.graphql");
        Document document = document(
                operation(
                        "SimpleQuery",
                        fieldWithDirectives(
                                "customers",
                                directives(
                                        directive("myDirective"),
                                        directive("deprecated", directiveArg("reason", "because"))),
                                field("id"),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }

    // (arguments, directives, subFields)
    @Test
    public void buildFieldWithArgumentsAndSubFieldsAndDirectivesTest() {
        String expectedRequest = Utils.getResourceFileContent("core/" +
                "directivesFieldWithArgumentsAndSubFields.graphql");
        Document document = document(
                operation(
                        "SimpleQuery",
                        fieldWithDirectives(
                                "customers",
                                args(arg("arg", "value")),
                                directives(
                                        directive("myDirective"),
                                        directive("deprecated", directiveArg("reason", "reasons"))),
                                field("id"),
                                field("name"))));
        String generatedRequest = document.build();
        AssertGraphQL.assertEquivalentGraphQLRequest(expectedRequest, generatedRequest);
    }
}

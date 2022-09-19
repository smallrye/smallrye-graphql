package io.smallrye.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.schema.model.Schema;

class SchemaTest {

    @Test
    void testSchemaWithDirectives() {
        GraphQLSchema graphQLSchema = createGraphQLSchema(
                Directive.class, IntArrayTestDirective.class, FieldDirective.class, ArgumentDirective.class,
                OperationDirective.class, TestTypeWithDirectives.class, DirectivesTestApi.class);

        GraphQLDirective typeDirective = graphQLSchema.getDirective("intArrayTestDirective");
        assertEquals("intArrayTestDirective", typeDirective.getName());
        assertEquals("test-description", typeDirective.getDescription());
        assertEquals(1, typeDirective.getArguments().size());
        assertEquals("[Int]", typeDirective.getArgument("value").getType().toString());

        GraphQLDirective fieldDirective = graphQLSchema.getDirective("fieldDirective");
        assertEquals("fieldDirective", fieldDirective.getName());
        assertNull(fieldDirective.getDescription());
        assertEquals(0, fieldDirective.getArguments().size());

        GraphQLObjectType testTypeWithDirectives = graphQLSchema.getObjectType("TestTypeWithDirectives");
        GraphQLDirective intArrayTestDirective = testTypeWithDirectives.getDirective("intArrayTestDirective");
        assertEquals("intArrayTestDirective", intArrayTestDirective.getName());
        GraphQLArgument argument = intArrayTestDirective.getArgument("value");
        assertEquals("value", argument.getName());
        assertArrayEquals(new Object[] { 1, 2, 3 }, (Object[]) argument.getArgumentValue().getValue());

        GraphQLFieldDefinition valueField = testTypeWithDirectives.getFieldDefinition("value");
        GraphQLDirective fieldDirectiveInstance = valueField.getDirective("fieldDirective");
        assertNotNull(fieldDirectiveInstance);

        assertOperationWithDirectives(graphQLSchema.getQueryType().getField("queryWithDirectives"));
        assertOperationWithDirectives(graphQLSchema.getMutationType().getField("mutationWithDirectives"));
        assertOperationWithDirectives(graphQLSchema.getSubscriptionType().getField("subscriptionWithDirectives"));

        String schemaString = new SchemaPrinter().print(graphQLSchema);
        assertSchemaContains(schemaString,
                "\"test-description\"\n" +
                        "directive @intArrayTestDirective(value: [Int]) on OBJECT | INTERFACE\n");
        assertSchemaEndsWith(schemaString, "" +
                "\"Mutation root\"\n" +
                "type Mutation {\n" +
                "  mutationWithDirectives(arg: [String] @argumentDirective): TestTypeWithDirectives @operationDirective\n" +
                "}\n" +
                "\n" +
                "\"Query root\"\n" +
                "type Query {\n" +
                "  queryWithDirectives(arg: [String] @argumentDirective): TestTypeWithDirectives @operationDirective\n" +
                "}\n" +
                "\n" +
                "\"Subscription root\"\n" +
                "type Subscription {\n" +
                "  subscriptionWithDirectives(arg: [String] @argumentDirective): TestTypeWithDirectives @operationDirective\n" +
                "}\n" +
                "\n" +
                "type TestTypeWithDirectives @intArrayTestDirective(value : [1, 2, 3]) {\n" +
                "  value: String @fieldDirective\n" +
                "}\n");
    }

    @Test
    void schemaWithEnumDirectives() {
        GraphQLSchema graphQLSchema = createGraphQLSchema(EnumDirective.class, EnumTestApi.class,
                EnumTestApi.EnumWithDirectives.class);

        GraphQLEnumType enumWithDirectives = graphQLSchema.getTypeAs("EnumWithDirectives");
        assertNotNull(enumWithDirectives.getDirective("enumDirective"),
                "Enum EnumWithDirectives should have directive @enumDirective");
        assertNotNull(enumWithDirectives.getValue("A").getDirective("enumDirective"),
                "Enum value EnumWithDirectives.A should have directive @enumDirective");

        String schemaString = new SchemaPrinter().print(graphQLSchema);
        assertSchemaEndsWith(schemaString, "" +
                "enum EnumWithDirectives @enumDirective {\n" +
                "  A @enumDirective\n" +
                "  B\n" +
                "}\n");
    }

    private GraphQLSchema createGraphQLSchema(Class<?>... api) {
        Schema schema = SchemaBuilder.build(scan(api));
        assertNotNull(schema, "Schema should not be null");
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema, true);
        assertNotNull(graphQLSchema, "GraphQLSchema should not be null");
        return graphQLSchema;
    }

    private static void assertSchemaContains(String schema, String snippet) {
        assertTrue(schema.contains(snippet), () -> "<<<\n" + schema + "\n>>> does not contain <<<\n" + snippet + "\n>>>");
    }

    private static void assertSchemaEndsWith(String schema, String end) {
        assertTrue(schema.endsWith(end), () -> "<<<\n" + schema + "\n>>> does not end with <<<\n" + end + "\n>>>");
    }

    private void assertOperationWithDirectives(GraphQLFieldDefinition operation) {
        String name = operation.getName();
        GraphQLDirective operationDirective = operation.getDirective("operationDirective");
        assertNotNull(operationDirective, () -> name + " should have directive @operationDirective");
        GraphQLDirective argumentDirective = operation.getArgument("arg").getDirective("argumentDirective");
        assertNotNull(argumentDirective, () -> "Argument arg of " + name + " should have directive @argumentDirective");
    }

    private IndexView scan(Class<?>... classes) {
        Indexer indexer = new Indexer();
        Stream.of(classes).forEach(cls -> index(indexer, cls));
        return indexer.complete();
    }

    private void index(Indexer indexer, Class<?> cls) {
        try {
            indexer.index(getResourceStream(cls));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getResourceStream(Class<?> type) {
        String name = type.getName().replace(".", "/") + ".class";
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }
}

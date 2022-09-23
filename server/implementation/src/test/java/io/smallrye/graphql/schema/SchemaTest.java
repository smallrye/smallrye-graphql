package io.smallrye.graphql.schema;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.stream.Stream;

import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.execution.TestConfig;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.spi.config.Config;

class SchemaTest {
    private final TestConfig config = (TestConfig) Config.get();

    @AfterEach
    void tearDown() {
        config.reset();
    }

    @Test
    void testSchemaWithDirectives() throws URISyntaxException, IOException {
        GraphQLSchema graphQLSchema = createGraphQLSchema(
                Directive.class, IntArrayTestDirective.class, FieldDirective.class, ArgumentDirective.class,
                OperationDirective.class, TestTypeWithDirectives.class, DirectivesTestApi.class, TestInterfaceDirective.class,
                TestInterfaceDirectiveImpl.class);

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
        assertArrayEquals(new Object[] { 1, 2, 3 }, argument.toAppliedArgument().getValue());

        GraphQLFieldDefinition valueField = testTypeWithDirectives.getFieldDefinition("value");
        GraphQLDirective fieldDirectiveInstance = valueField.getDirective("fieldDirective");
        assertNotNull(fieldDirectiveInstance);

        assertOperationWithDirectives(graphQLSchema.getQueryType().getField("queryWithDirectives"));
        assertOperationWithDirectives(graphQLSchema.getMutationType().getField("mutationWithDirectives"));
        assertOperationWithDirectives(graphQLSchema.getSubscriptionType().getField("subscriptionWithDirectives"));

        String actualSchema = new SchemaPrinter().print(graphQLSchema);
        String expectedSchema = Files
                .readString(new File(SchemaTest.class.getResource("/schemaTest.graphql").toURI()).toPath());
        Assertions.assertEquals(expectedSchema, actualSchema);
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

        assertSchemaEndsWith(graphQLSchema, "" +
                "enum EnumWithDirectives @enumDirective {\n" +
                "  A @enumDirective\n" +
                "  B\n" +
                "}\n");
    }

    @Test
    void schemaWithInputDirectives() {
        GraphQLSchema graphQLSchema = createGraphQLSchema(InputDirective.class, InputTestApi.class,
                InputTestApi.InputWithDirectives.class);

        GraphQLInputObjectType inputWithDirectives = graphQLSchema.getTypeAs("InputWithDirectivesInput");
        assertNotNull(inputWithDirectives.getDirective("inputDirective"),
                "Input type InputWithDirectivesInput should have directive @inputDirective");
        assertNotNull(inputWithDirectives.getField("foo").getDirective("inputDirective"),
                "Input type field InputWithDirectivesInput.foo should have directive @inputDirective");
        assertNotNull(inputWithDirectives.getField("bar").getDirective("inputDirective"),
                "Input type field InputWithDirectivesInput.bar should have directive @inputDirective");

        assertSchemaEndsWith(graphQLSchema, "" +
                "input InputWithDirectivesInput @inputDirective {\n" +
                "  bar: Int! @inputDirective\n" +
                "  foo: Int! @inputDirective\n" +
                "}\n");
    }

    @Test
    void testSchemaWithFederationDisabled() {
        GraphQLSchema graphQLSchema = createGraphQLSchema(Directive.class, Key.class, TestTypeWithFederation.class,
                FederationTestApi.class);

        assertSchemaEndsWith(graphQLSchema, "\n" +
                "\"Query root\"\n" +
                "type Query {\n" +
                "  testTypeWithFederation(arg: String): TestTypeWithFederation\n" +
                "}\n" +
                "\n" +
                "type TestTypeWithFederation @key(fields : [\"id\"]) {\n" +
                "  id: String\n" +
                "}\n");
    }

    @Test
    void testSchemaWithFederation() {
        config.federationEnabled = true;
        GraphQLSchema graphQLSchema = createGraphQLSchema(Directive.class, Key.class, TestTypeWithFederation.class,
                FederationTestApi.class);

        assertSchemaEndsWith(graphQLSchema, "\n" +
                "union _Entity = TestTypeWithFederation\n" +
                "\n" +
                "\"Query root\"\n" +
                "type Query {\n" +
                "  _entities(representations: [_Any!]!): [_Entity]!\n" +
                "  _service: _Service!\n" +
                "  testTypeWithFederation(arg: String): TestTypeWithFederation\n" +
                "}\n" +
                "\n" +
                "type TestTypeWithFederation @key(fields : [\"id\"]) {\n" +
                "  id: String\n" +
                "}\n" +
                "\n" +
                "type _Service {\n" +
                "  sdl: String!\n" +
                "}\n");
    }

    private GraphQLSchema createGraphQLSchema(Class<?>... api) {
        Schema schema = SchemaBuilder.build(scan(api));
        assertNotNull(schema, "Schema should not be null");
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema, true);
        assertNotNull(graphQLSchema, "GraphQLSchema should not be null");
        return graphQLSchema;
    }

    private static void assertSchemaEndsWith(GraphQLSchema schema, String end) {
        String schemaString = new SchemaPrinter().print(schema);
        assertSchemaEndsWith(schemaString, end);
    }

    private static void assertSchemaEndsWith(String schema, String end) {
        // assertEquals(schema, end); // this is convenient for debugging, as the IDE can show the diff
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

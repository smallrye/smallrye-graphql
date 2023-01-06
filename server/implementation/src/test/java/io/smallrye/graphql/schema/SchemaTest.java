package io.smallrye.graphql.schema;

import static graphql.Scalars.GraphQLString;
import static graphql.introspection.Introspection.DirectiveLocation.INTERFACE;
import static graphql.introspection.Introspection.DirectiveLocation.OBJECT;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Repeatable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.EnumSet;
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
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLUnionType;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.api.federation.Key.Keys;
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
        var schemaUri = requireNonNull(SchemaTest.class.getResource("/schemaTest.graphql")).toURI();
        String expectedSchema = Files.readString(new File(schemaUri).toPath());
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
        assertNull(enumWithDirectives.getValue("B").getDirective("enumDirective"),
                "Enum value EnumWithDirectives.B should not have directive @enumDirective");
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
    }

    @Test
    void testSchemaWithFederationDisabled() {
        config.federationEnabled = false;
        // need to set it as system property because the SchemaBuilder doesn't have access to the Config object
        System.setProperty("smallrye.graphql.federation.enabled", "false");

        GraphQLSchema graphQLSchema = createGraphQLSchema(Directive.class, Key.class, Keys.class,
                TestTypeWithFederation.class, FederationTestApi.class);

        assertNull(graphQLSchema.getDirective("key"));
        assertNull(graphQLSchema.getType("_Entity"));

        GraphQLObjectType queryRoot = graphQLSchema.getQueryType();
        assertEquals(1, queryRoot.getFields().size());
        assertNull(queryRoot.getField("_entities"));
        assertNull(queryRoot.getField("_service"));

        GraphQLFieldDefinition query = queryRoot.getField("testTypeWithFederation");
        assertEquals(1, query.getArguments().size());
        assertEquals(GraphQLString, query.getArgument("arg").getType());
        assertEquals("TestTypeWithFederation", ((GraphQLObjectType) query.getType()).getName());

        GraphQLObjectType type = graphQLSchema.getObjectType("TestTypeWithFederation");
        assertEquals(0, type.getDirectives().size());
        assertEquals(3, type.getFields().size());
        assertEquals("id", type.getFields().get(0).getName());
        assertEquals(GraphQLString, type.getFields().get(0).getType());
        assertEquals("type", type.getFields().get(1).getName());
        assertEquals(GraphQLString, type.getFields().get(1).getType());
        assertEquals("value", type.getFields().get(2).getName());
        assertEquals(GraphQLString, type.getFields().get(2).getType());

        assertNull(graphQLSchema.getObjectType("_Service"));
    }

    @Test
    void testSchemaWithFederationEnabled() {
        config.federationEnabled = true;
        // need to set it as system property because the SchemaBuilder doesn't have access to the Config object
        System.setProperty("smallrye.graphql.federation.enabled", "true");
        try {
            GraphQLSchema graphQLSchema = createGraphQLSchema(Repeatable.class, Directive.class, Key.class, Keys.class,
                    TestTypeWithFederation.class, FederationTestApi.class);

            GraphQLDirective keyDirective = graphQLSchema.getDirective("key");
            assertEquals("key", keyDirective.getName());
            assertTrue(keyDirective.isRepeatable());
            assertEquals(
                    "Designates an object type as an entity and specifies its key fields " +
                            "(a set of fields that the subgraph can use to uniquely identify any instance " +
                            "of the entity). You can apply multiple @key directives to a single entity " +
                            "(to specify multiple valid sets of key fields).",
                    keyDirective.getDescription());
            assertEquals(EnumSet.of(OBJECT, INTERFACE), keyDirective.validLocations());
            assertEquals(1, keyDirective.getArguments().size());
            assertEquals("String", ((GraphQLScalarType) keyDirective.getArgument("fields").getType()).getName());

            GraphQLUnionType entityType = (GraphQLUnionType) graphQLSchema.getType("_Entity");
            assertNotNull(entityType);
            assertEquals(1, entityType.getTypes().size());
            assertEquals(TestTypeWithFederation.class.getSimpleName(), entityType.getTypes().get(0).getName());

            GraphQLObjectType queryRoot = graphQLSchema.getQueryType();
            assertEquals(3, queryRoot.getFields().size());

            GraphQLFieldDefinition entities = queryRoot.getField("_entities");
            assertEquals(1, entities.getArguments().size());
            assertEquals("[_Any!]!", entities.getArgument("representations").getType().toString());
            assertEquals("[_Entity]!", entities.getType().toString());

            GraphQLFieldDefinition service = queryRoot.getField("_service");
            assertEquals(0, service.getArguments().size());
            assertEquals("_Service!", service.getType().toString());

            GraphQLFieldDefinition query = queryRoot.getField("testTypeWithFederation");
            assertEquals(1, query.getArguments().size());
            assertEquals(GraphQLString, query.getArgument("arg").getType());
            assertEquals("TestTypeWithFederation", ((GraphQLObjectType) query.getType()).getName());

            GraphQLObjectType type = graphQLSchema.getObjectType("TestTypeWithFederation");
            assertEquals(2, type.getDirectives().size());
            assertKeyDirective(type.getDirectives().get(0), "id");
            assertKeyDirective(type.getDirectives().get(1), "type id");
            assertEquals(3, type.getFields().size());
            assertEquals("id", type.getFields().get(0).getName());
            assertEquals(GraphQLString, type.getFields().get(0).getType());
            assertEquals("type", type.getFields().get(1).getName());
            assertEquals(GraphQLString, type.getFields().get(1).getType());
            assertEquals("value", type.getFields().get(2).getName());
            assertEquals(GraphQLString, type.getFields().get(2).getType());

            GraphQLObjectType serviceType = graphQLSchema.getObjectType("_Service");
            assertEquals(1, serviceType.getFields().size());
            assertEquals("sdl", serviceType.getFields().get(0).getName());
            assertEquals("String!", serviceType.getFields().get(0).getType().toString());
        } finally {
            System.clearProperty("smallrye.graphql.federation.enabled");
        }
    }

    private static void assertKeyDirective(GraphQLDirective graphQLDirective, String value) {
        assertEquals("key", graphQLDirective.getName());
        assertEquals(1, graphQLDirective.getArguments().size());
        assertEquals("fields", graphQLDirective.getArguments().get(0).getName());
        assertEquals(value, graphQLDirective.getArguments().get(0).toAppliedArgument().getArgumentValue().getValue());
    }

    private GraphQLSchema createGraphQLSchema(Class<?>... api) {
        Schema schema = SchemaBuilder.build(scan(api));
        assertNotNull(schema, "Schema should not be null");
        GraphQLSchema graphQLSchema = Bootstrap.bootstrap(schema, true);
        assertNotNull(graphQLSchema, "GraphQLSchema should not be null");
        return graphQLSchema;
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

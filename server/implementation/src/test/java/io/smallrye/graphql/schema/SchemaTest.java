package io.smallrye.graphql.schema;

import static graphql.Scalars.GraphQLString;
import static graphql.introspection.Introspection.DirectiveLocation.INTERFACE;
import static graphql.introspection.Introspection.DirectiveLocation.OBJECT;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Repeatable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.security.RolesAllowed;

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
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLUnionType;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.api.OneOf;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.api.federation.Key.Keys;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.execution.TestConfig;
import io.smallrye.graphql.schema.directiveswithenumvalues.MyEnum;
import io.smallrye.graphql.schema.directiveswithenumvalues.MyEnumValueDirective;
import io.smallrye.graphql.schema.directiveswithenumvalues.MyObject;
import io.smallrye.graphql.schema.directiveswithenumvalues.SomeApi;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.schema.rolesallowedschemas.Customer;
import io.smallrye.graphql.schema.rolesallowedschemas.RolesSchema1;
import io.smallrye.graphql.schema.rolesallowedschemas.RolesSchema2;
import io.smallrye.graphql.schema.rolesallowedschemas.RolesSchema3;
import io.smallrye.graphql.schema.schemadirectives.NonRepeatableSchemaDirective;
import io.smallrye.graphql.schema.schemadirectives.OutputDirective;
import io.smallrye.graphql.schema.schemadirectives.RepeatableSchemaDirective;
import io.smallrye.graphql.schema.schemadirectives.Schema1;
import io.smallrye.graphql.schema.schemadirectives.Schema2;
import io.smallrye.graphql.schema.schemadirectives.Schema3;
import io.smallrye.graphql.schema.schemadirectives.Schema4;
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
    void testSchemaWithEnumValueDirectives() throws URISyntaxException, IOException {
        GraphQLSchema graphQLSchema = createGraphQLSchema(MyEnum.class, MyEnumValueDirective.class, EnumDirective.class,
                MyObject.class, SomeApi.class);

        GraphQLDirective typeDirective = graphQLSchema.getDirective("myEnumValueDirective");
        assertEquals("myEnumValueDirective", typeDirective.getName());
        assertEquals(1, typeDirective.getArguments().size());
        assertEquals("MyEnum!", typeDirective.getArgument("value").getType().toString());

        GraphQLFieldDefinition fieldWithEnumValueDirective = graphQLSchema
                .getObjectType("MyObject")
                .getFieldDefinition("name");

        GraphQLDirective enumValueDirectiveInstance = fieldWithEnumValueDirective.getDirective("myEnumValueDirective");
        assertEquals("myEnumValueDirective", enumValueDirectiveInstance.getName());
        GraphQLArgument argument = enumValueDirectiveInstance.getArgument("value");
        assertEquals("value", argument.getName());
        assertEquals(MyEnum.SOME.toString(), argument.toAppliedArgument().getValue());

        GraphQLEnumType enumType = graphQLSchema.getTypeAs("MyEnum");
        assertNotNull(enumType.getDirective("enumDirective"),
                "Enum MyEnum should have directive @enumDirective");
    }

    @Test
    void schemaWithEnumDirectives() {
        GraphQLSchema graphQLSchema = createGraphQLSchema(EnumDirective.class, EnumTestApi.class,
                EnumTestApi.EnumWithDirectives.class);

        GraphQLEnumType enumWithDirectives = graphQLSchema.getTypeAs("EnumWithDirectives");
        assertNotNull(enumWithDirectives.getDirective("enumDirective"),
                "Enum EnumWithDirectives should have directive @enumDirective");
        assertEquals("EnumWithDirectives description", enumWithDirectives.getDescription());
        assertNotNull(enumWithDirectives.getValue("A").getDirective("enumDirective"),
                "Enum value EnumWithDirectives.A should have directive @enumDirective");
        assertEquals("A description", enumWithDirectives.getValue("A").getDescription());
        assertNull(enumWithDirectives.getValue("B").getDirective("enumDirective"),
                "Enum value EnumWithDirectives.B should not have directive @enumDirective");
        assertNull(enumWithDirectives.getValue("B").getDescription());
    }

    @Test
    void schemaWithUnionDirectives() {
        GraphQLSchema graphQLSchema = createGraphQLSchema(UnionDirective.class, UnionTestApi.class,
                UnionTestApi.SomeUnion.class, UnionTestApi.SomeClass.class);

        GraphQLUnionType unionWithDirectives = graphQLSchema.getTypeAs("SomeUnion");
        List<GraphQLDirective> unionDirectives = unionWithDirectives.getDirectives("unionDirective");
        assertFalse(unionDirectives.isEmpty(),
                "Union SomeUnion should have directive @unionDirective");
        assertEquals(3, unionDirectives.size(), "Union SomeUnion should have 3 @unionDirective instances");
        Set<String> expectedDirectivesArgValues = new HashSet<>(Arrays.asList("A", "B", "C"));
        unionDirectives.forEach(directive -> directive.getArguments()
                .forEach(argument -> assertFalse(expectedDirectivesArgValues.add(argument.toAppliedArgument().getValue()),
                        "Unexpected directive argument value")));
        assertTrue(unionWithDirectives.getDirectives("InputDirective").isEmpty(),
                "Union SomeUnion should not have a directive @inputDirective");
        assertEquals("Union description", unionWithDirectives.getDescription());
    }

    @Test
    void schemaWithInputDirectives() {
        GraphQLSchema graphQLSchema = createGraphQLSchema(InputDirective.class, OutputDirective.class, InputTestApi.class,
                InputTestApi.InputWithDirectives.class, InputTestApi.SomeObject.class);

        GraphQLInputObjectType inputWithDirectives = graphQLSchema.getTypeAs("InputWithDirectivesInput");
        assertNotNull(inputWithDirectives.getDirective("inputDirective"),
                "Input type InputWithDirectivesInput should have directive @inputDirective");
        assertEquals("InputType description", inputWithDirectives.getDescription());
        assertNotNull(inputWithDirectives.getField("foo").getDirective("inputDirective"),
                "Input type field InputWithDirectivesInput.foo should have directive @inputDirective");
        assertEquals("InputTypeField description", inputWithDirectives.getField("foo").getDescription());
        assertNotNull(inputWithDirectives.getField("bar").getDirective("inputDirective"),
                "Input type field InputWithDirectivesInput.bar should have directive @inputDirective");

        GraphQLInputObjectType outputAsInputWithDirectives = graphQLSchema.getTypeAs("SomeObjectInput");
        assertNull(outputAsInputWithDirectives.getDirective("outputDirective"),
                "Input type SomeObject should not have directive @outputDirective");
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
            assertEquals(2, keyDirective.getArguments().size());
            assertEquals("String",
                    ((GraphQLScalarType) ((GraphQLNonNull) keyDirective.getArgument("fields").getType()).getWrappedType())
                            .getName());
            assertEquals("Boolean",
                    ((GraphQLScalarType) keyDirective.getArgument("resolvable").getType()).getName());

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
            assertKeyDirective(type.getDirectives().get(0), "id", null);
            assertKeyDirective(type.getDirectives().get(1), "type id", true);
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

    @Test
    void testSchemasWithValidSchemaDirectives() {
        GraphQLSchema graphQLSchema = createGraphQLSchema(Directive.class, Repeatable.class, RepeatableSchemaDirective.class,
                NonRepeatableSchemaDirective.class, Schema1.class, Schema2.class, Schema3.class);
        assertEquals(graphQLSchema.getSchemaAppliedDirectives().size(), 5);
        Set<String> expectedArgValues = new HashSet<>(Arrays.asList("name1", "name2", "name3", "name4"));
        graphQLSchema.getSchemaAppliedDirectives().stream()
                .filter(graphQLAppliedDirective -> graphQLAppliedDirective.getName().equals("repeatableSchemaDirective"))
                .collect(Collectors.toList()).forEach(composeDirective -> composeDirective.getArguments()
                        .forEach(argument -> assertTrue(!expectedArgValues.add(argument.getValue()),
                                "Unexpected directive argument value")));
        assertEquals("Schema description", graphQLSchema.getDescription());
    }

    @Test
    void testSchemasWithInvalidSchemaDirectives() {
        Exception exception = assertThrows(SchemaBuilderException.class,
                () -> createGraphQLSchema(Directive.class, Repeatable.class, RepeatableSchemaDirective.class,
                        NonRepeatableSchemaDirective.class, Schema3.class, Schema4.class));
        String expectedMessage = "The @nonRepeatableSchemaDirective directive is not repeatable, but was used more than once in the GraphQL schema.";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testSchemasWithRolesAllowedDirectives() {
        GraphQLSchema graphQLSchema = createGraphQLSchema(Customer.class, RolesAllowed.class, RolesSchema1.class,
                RolesSchema2.class,
                RolesSchema3.class);

        // QUERY ROOT
        GraphQLObjectType queryRoot = graphQLSchema.getQueryType();
        assertEquals(5, queryRoot.getFields().size());

        GraphQLFieldDefinition helloQuery = queryRoot.getField("hello");
        assertRolesAllowedDirective(helloQuery, "admin");

        GraphQLFieldDefinition anotherHelloQuery = queryRoot.getField("anotherHello");
        assertRolesAllowedDirective(anotherHelloQuery, null);

        GraphQLFieldDefinition moneyQuery = queryRoot.getField("money");
        assertRolesAllowedDirective(moneyQuery, "employee");

        GraphQLFieldDefinition adminMoneyQuery = queryRoot.getField("adminMoney");
        assertRolesAllowedDirective(adminMoneyQuery, "admin");

        GraphQLFieldDefinition doNothingQuery = queryRoot.getField("doNothing");
        assertRolesAllowedDirective(doNothingQuery, null);

        // MUTATION ROOT
        GraphQLObjectType mutationRoot = graphQLSchema.getMutationType();
        assertEquals(3, mutationRoot.getFields().size());

        GraphQLFieldDefinition createHelloMutation = mutationRoot.getField("createHello");
        assertRolesAllowedDirective(createHelloMutation, "admin");

        GraphQLFieldDefinition createCustomerMutation = mutationRoot.getField("createCustomer");
        assertRolesAllowedDirective(createCustomerMutation, "employee");

        GraphQLFieldDefinition createNothingMutation = mutationRoot.getField("createNothing");
        assertRolesAllowedDirective(createNothingMutation, null);

        // SUBSCRIPTION TYPE
        GraphQLObjectType subscriptionType = graphQLSchema.getSubscriptionType();
        assertEquals(3, subscriptionType.getFields().size());

        GraphQLFieldDefinition helloCreatedSubscription = subscriptionType.getField("helloCreated");
        assertRolesAllowedDirective(helloCreatedSubscription, "basic");

        GraphQLFieldDefinition customerCreatedSubscription = subscriptionType.getField("customerCreated");
        assertRolesAllowedDirective(customerCreatedSubscription, "employee");

        GraphQLFieldDefinition nothingCreatedSubscription = subscriptionType.getField("nothingCreated");
        assertRolesAllowedDirective(nothingCreatedSubscription, null);

        // SOURCE METHODS
        GraphQLObjectType type = graphQLSchema.getObjectType("Customer");
        assertEquals(0, type.getDirectives().size());
        assertEquals(4, type.getFields().size());
        assertEquals(Set.of("name", "password", "nothingPassword", "adminPassword"),
                type.getFields().stream().map(GraphQLFieldDefinition::getName).collect(Collectors.toSet()));

        GraphQLFieldDefinition nameField = type.getField("name");
        assertEquals(GraphQLString, nameField.getType());
        assertRolesAllowedDirective(nameField, null);

        GraphQLFieldDefinition passwordField = type.getField("password");
        assertEquals(GraphQLString, passwordField.getType());
        assertRolesAllowedDirective(passwordField, "employee");

        GraphQLFieldDefinition adminPasswordField = type.getField("adminPassword");
        assertEquals(GraphQLString, adminPasswordField.getType());
        assertRolesAllowedDirective(adminPasswordField, "admin");
    }

    @Test
    void testSchemasWithOneOfDirective() {
        GraphQLSchema graphQLSchema = createGraphQLSchema(OneOfSchema.class, OneOf.class, OneOfSchema.SomeClass.class);

        GraphQLObjectType queryRoot = graphQLSchema.getQueryType();
        assertEquals(1, queryRoot.getFields().size());

        GraphQLFieldDefinition someQuery = queryRoot.getField("someQuery");
        assertNotNull(someQuery);

        GraphQLObjectType someClassOutput = graphQLSchema.getTypeAs("SomeClass");
        assertNotNull(someClassOutput);
        assertEquals(0, someClassOutput.getDirectives().size());

        GraphQLInputObjectType someClassInput = graphQLSchema.getTypeAs("SomeClassInput");
        assertNotNull(someClassInput);
        assertEquals(1, someClassInput.getDirectives().size());
        GraphQLDirective oneOfDirective = someClassInput.getDirective("oneOf");
        assertNotNull(oneOfDirective);
        assertEquals(0, oneOfDirective.getArguments().size());

        // vvv directive header, since the directiveInstance does not have Description vvv
        oneOfDirective = graphQLSchema.getDirective("oneOf");
        assertNotNull(oneOfDirective);
        assertEquals(0, oneOfDirective.getArguments().size());
        assertEquals("Indicates an Input Object is a OneOf Input Object.", oneOfDirective.getDescription());
    }

    private void assertKeyDirective(GraphQLDirective graphQLDirective, String fieldsValue, Boolean resolvableValue) {
        assertEquals("key", graphQLDirective.getName());
        assertEquals(2, graphQLDirective.getArguments().size());
        assertEquals("fields", graphQLDirective.getArguments().get(0).getName());
        assertEquals("resolvable", graphQLDirective.getArguments().get(1).getName());
        assertEquals(fieldsValue, graphQLDirective.getArguments().get(0).toAppliedArgument().getArgumentValue().getValue());
        assertEquals(resolvableValue, graphQLDirective.getArguments().get(1).toAppliedArgument().getArgumentValue().getValue());
        assertEquals(true, graphQLDirective.getArguments().get(1).getArgumentDefaultValue().getValue());
    }

    private void assertRolesAllowedDirective(GraphQLFieldDefinition field, String roleValue) {
        assertNotNull(field);

        if (Objects.isNull(roleValue)) {
            assertTrue(field.getDirectives().isEmpty());
            return;
        }
        assertEquals(1, field.getDirectives().size());
        assertEquals("rolesAllowed", field.getDirectives().get(0).getName());
        assertEquals(1, field.getDirective("rolesAllowed").getArguments().size());
        assertEquals("value", field.getDirective("rolesAllowed").getArguments().get(0).getName());
        assertEquals(roleValue,
                field.getDirective("rolesAllowed").getArgument("value").toAppliedArgument().getArgumentValue().getValue());

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

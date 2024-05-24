package io.smallrye.graphql.schema;

import static graphql.Scalars.GraphQLString;
import static graphql.introspection.Introspection.DirectiveLocation.INTERFACE;
import static graphql.introspection.Introspection.DirectiveLocation.OBJECT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Repeatable;
import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLUnionType;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.api.federation.Key.Keys;

class SchemaFederationEnabledTest extends SchemaTestBase {

    @Test
    void testSchemaWithFederationEnabled() {
        config.federationEnabled = true;
        // need to set it as system property because the SchemaBuilder doesn't have access to the Config object
        System.setProperty("smallrye.graphql.federation.enabled", "true");
        try {
            GraphQLSchema graphQLSchema = createGraphQLSchema(Repeatable.class, Directive.class, Key.class, Keys.class,
                    FieldSet.class, TestTypeWithFederation.class, FederationTestApi.class, TestInterfaceWitFederation.class);

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
            assertEquals("FieldSet",
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
}

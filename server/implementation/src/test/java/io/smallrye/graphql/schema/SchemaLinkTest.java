package io.smallrye.graphql.schema;

import static io.smallrye.graphql.api.federation.link.Link.FEDERATION_SPEC_LATEST_URL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import graphql.schema.GraphQLAppliedDirective;
import graphql.schema.GraphQLAppliedDirectiveArgument;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.api.Directive;
import io.smallrye.graphql.api.federation.Authenticated;
import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.External;
import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.Inaccessible;
import io.smallrye.graphql.api.federation.Override;
import io.smallrye.graphql.api.federation.Requires;
import io.smallrye.graphql.api.federation.Tag;
import io.smallrye.graphql.api.federation.link.Import;
import io.smallrye.graphql.api.federation.link.Link;
import io.smallrye.graphql.api.federation.link.Purpose;
import io.smallrye.graphql.api.federation.policy.Policy;
import io.smallrye.graphql.api.federation.policy.PolicyGroup;
import io.smallrye.graphql.api.federation.policy.PolicyItem;
import io.smallrye.graphql.schema.link.Book;
import io.smallrye.graphql.schema.link.CustomDirective;
import io.smallrye.graphql.schema.link.Link1Api;
import io.smallrye.graphql.schema.link.Link2Api;
import io.smallrye.graphql.schema.link.Link3Api;
import io.smallrye.graphql.schema.link.Link4Api;
import io.smallrye.graphql.schema.link.Link5Api;
import io.smallrye.graphql.schema.link.Link6Api;
import io.smallrye.graphql.schema.link.Link7Api;
import io.smallrye.graphql.schema.link.Link8Api;

class SchemaLinkTest extends SchemaTestBase {

    @BeforeEach
    public void init() {
        config.federationEnabled = true;
        // need to set it as system property because the SchemaBuilder doesn't have access to the Config object
        System.setProperty("smallrye.graphql.federation.enabled", "true");
    }

    @AfterEach
    void tearDown() {
        config.reset();
        System.clearProperty("smallrye.graphql.federation.enabled");
    }

    @Test
    void testSchemaWithMultipleFederationSpec() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> createGraphQLSchema(Directive.class, Link.class, Import.class, Purpose.class, Link1Api.class));
        String expectedPattern = "Multiple @link directives that import Federation spec found on schema: .*";
        assertTrue(exception.getMessage().matches(expectedPattern),
                "Exception message does not match expected pattern");
    }

    @Test
    void testSchemaWithOldFederationSpecVersion() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> createGraphQLSchema(Directive.class, Link.class, Import.class, Purpose.class, Link2Api.class));
        String expectedMessage = "Federation v2.6 feature @policy imported using old Federation v2.1 version";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testSchemaWithUnsupportedFederationSpecVersion() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> createGraphQLSchema(Directive.class, Link.class, Import.class, Purpose.class, Link3Api.class));
        String expectedMessage = "Specified federation spec = https://specs.apollo.dev/federation/v100.20 is " +
                "currently not supported";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testSchemaWithImportMismatch() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> createGraphQLSchema(Directive.class, Link.class, Import.class, Purpose.class, Link4Api.class));
        String expectedMessage = "Import name '@authenticated' and alias 'myAuthenticated' on on @link directive must" +
                " be of the same type: either both directives or both types";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testSchemaWithNonexistentImport() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> createGraphQLSchema(Directive.class, Link.class, Import.class, Purpose.class, Link5Api.class));
        String expectedMessage = "Import key @nothing is not present in the Federation spec https://specs.apollo" +
                ".dev/federation/v2.7";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testSchemaWithForbiddenImport() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> createGraphQLSchema(Directive.class, Link.class, Import.class, Purpose.class, Link6Api.class));
        String expectedMessage = "Import key Import should not be imported within @link directive itself";
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void testSchemaWithImportAs() {
        // Extends and Override are not actually used, this is on purpose
        GraphQLSchema graphQLSchema = createGraphQLSchema(Directive.class, Link.class, Import.class, Purpose.class,
                Authenticated.class, CustomDirective.class, External.class, Inaccessible.class, Policy.class,
                PolicyGroup.class, Extends.class, FieldSet.class, Override.class, PolicyItem.class, Requires.class,
                Tag.class, Book.class, Link7Api.class);

        GraphQLAppliedDirective specLinkDirective = graphQLSchema.getSchemaAppliedDirectives().get(0);
        assertEquals("link", specLinkDirective.getName());
        assertEquals(4, specLinkDirective.getArguments().size());
        GraphQLAppliedDirectiveArgument urlArgument = specLinkDirective.getArgument("url");
        assertEquals("String!", urlArgument.getType().toString());
        assertEquals(FEDERATION_SPEC_LATEST_URL, urlArgument.getValue());
        GraphQLAppliedDirectiveArgument asArgument = specLinkDirective.getArgument("as");
        assertEquals("String", ((GraphQLScalarType) asArgument.getType()).getName());
        assertNull(asArgument.getValue());
        GraphQLAppliedDirectiveArgument forArgument = specLinkDirective.getArgument("for");
        assertEquals("link__Purpose", ((GraphQLEnumType) forArgument.getType()).getName());
        assertEquals(Purpose.EXECUTION.toString(), forArgument.getValue());

        testSchemawithAs(graphQLSchema, "federation");
    }

    @Test
    void testSchemaWithAsAndImportAs() {
        // Extends and Override are not actually used, this is on purpose
        GraphQLSchema graphQLSchema = createGraphQLSchema(Directive.class, Link.class, Import.class, Purpose.class,
                Authenticated.class, CustomDirective.class, External.class, Inaccessible.class, Policy.class,
                PolicyGroup.class, Extends.class, FieldSet.class, Override.class, PolicyItem.class, Requires.class,
                Tag.class, Book.class, Link8Api.class);

        GraphQLAppliedDirective specLinkDirective = graphQLSchema.getSchemaAppliedDirectives().get(0);
        assertEquals("link", specLinkDirective.getName());
        assertEquals(4, specLinkDirective.getArguments().size());
        GraphQLAppliedDirectiveArgument urlArgument = specLinkDirective.getArgument("url");
        assertEquals("String!", urlArgument.getType().toString());
        assertEquals(FEDERATION_SPEC_LATEST_URL, urlArgument.getValue());
        GraphQLAppliedDirectiveArgument asArgument = specLinkDirective.getArgument("as");
        assertEquals("String", ((GraphQLScalarType) asArgument.getType()).getName());
        assertEquals("smallrye", asArgument.getValue());
        GraphQLAppliedDirectiveArgument forArgument = specLinkDirective.getArgument("for");
        assertEquals("link__Purpose", ((GraphQLEnumType) forArgument.getType()).getName());
        assertEquals(Purpose.EXECUTION.toString(), forArgument.getValue());

        testSchemawithAs(graphQLSchema, "smallrye");
    }

    private void testSchemawithAs(GraphQLSchema graphQLSchema, String prefix) {
        GraphQLAppliedDirective customLinkDirective = graphQLSchema.getSchemaAppliedDirectives().get(1);
        assertEquals("link", customLinkDirective.getName());
        assertEquals(4, customLinkDirective.getArguments().size());
        GraphQLAppliedDirectiveArgument urlArgument = customLinkDirective.getArgument("url");
        assertEquals("String!", urlArgument.getType().toString());
        assertEquals("https://smallrye.io/custom/v1.0", urlArgument.getValue());

        GraphQLDirective customDirective = graphQLSchema.getDirective("smallryeCustom");
        assertEquals("smallryeCustom", customDirective.getName());

        GraphQLDirective authenticatedDirective = graphQLSchema.getDirective("authenticated");
        assertEquals("authenticated", authenticatedDirective.getName());

        GraphQLDirective externalDirective = graphQLSchema.getDirective("external");
        assertEquals("external", externalDirective.getName());

        GraphQLDirective inaccessibleDirective = graphQLSchema.getDirective("inaccessible");
        assertEquals("inaccessible", inaccessibleDirective.getName());

        GraphQLDirective policyDirective = graphQLSchema.getDirective("myPolicy");
        assertEquals("myPolicy", policyDirective.getName());
        assertEquals(1, policyDirective.getArguments().size());
        assertEquals("[[" + prefix + "__Policy!]!]!", policyDirective.getArgument("policies").getType().toString());

        GraphQLDirective tagDirective = graphQLSchema.getDirective("newTag");
        assertEquals("newTag", tagDirective.getName());
        assertEquals(1, tagDirective.getArguments().size());
        assertEquals("String!", tagDirective.getArgument("name").getType().toString());

        GraphQLDirective requiresDirective = graphQLSchema.getDirective("requires");
        assertEquals("requires", requiresDirective.getName());
        assertEquals(1, requiresDirective.getArguments().size());
        assertEquals("Field!", requiresDirective.getArgument("fields").getType().toString());

        GraphQLDirective extendsDirective = graphQLSchema.getDirective(prefix + "__extends");
        assertEquals(prefix + "__extends", extendsDirective.getName());

        GraphQLDirective overrideDirective = graphQLSchema.getDirective(prefix + "__override");
        assertEquals(prefix + "__override", overrideDirective.getName());
        assertEquals(2, overrideDirective.getArguments().size());
        assertEquals("String!", overrideDirective.getArgument("from").getType().toString());
        assertEquals("String", ((GraphQLScalarType) overrideDirective.getArgument("label").getType()).getName());

        GraphQLObjectType bookType = graphQLSchema.getObjectType("Book");

        GraphQLDirective bookAuthenticatedDirective = bookType.getDirective("authenticated");
        assertEquals("authenticated", bookAuthenticatedDirective.getName());

        GraphQLDirective bookPolicyDirective = bookType.getDirective("myPolicy");
        assertEquals("myPolicy", bookPolicyDirective.getName());
        assertEquals(1, bookPolicyDirective.getArguments().size());
        GraphQLArgument policiesArgument = bookPolicyDirective.getArgument("policies");
        assertEquals("[[" + prefix + "__Policy!]!]!", policiesArgument.getType().toString());
        ArrayList<ArrayList<String>> expectedPolicies = new ArrayList<>() {
            {
                add(new ArrayList<>(Arrays.asList("policy1", "policy2")));
                add(new ArrayList<>(Arrays.asList("policy3")));
            }
        };
        assertEquals(expectedPolicies, policiesArgument.toAppliedArgument().getValue());

        GraphQLScalarType importType = (GraphQLScalarType) graphQLSchema.getType("link__Import");
        assertEquals("link__Import", importType.getName());

        GraphQLEnumType purposeType = (GraphQLEnumType) graphQLSchema.getType("link__Purpose");
        assertEquals("link__Purpose", purposeType.getName());
    }
}

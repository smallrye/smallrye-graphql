package io.smallrye.graphql.tests.client.typesafe.directives;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.tests.client.typesafe.directives.model.SomeClass;

@GraphQLApi
public class ServerApi {

    private static String EXPECTED_QUERY = "query querySomeClass($someObject: SomeClassInput, $simpleType: Boolean! @variableDefinitionDirective @variableDefinitionDirective(fields: \"a\")) { querySomeClass(someObject: $someObject, simpleType: $simpleType) @fieldDirective(fields: [1]) @fieldDirective(fields: [2, 3]) {id @fieldDirective(fields: [4]) number} }";
    @Inject
    SmallRyeContext context;

    @Query
    public SomeClass getQuerySomeClass(SomeClass someObject, boolean simpleType) {
        if (!context.getQuery().equals(EXPECTED_QUERY)) {
            throw new RuntimeException("Queries do not match");
        }
        return someObject;
    }
}

package io.smallrye.graphql.schema;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class DirectivesTestApi {
    @Query
    public TestTypeWithDirectives queryWithDirectives(@ArgumentDirective List<String> arg) {
        return null;
    }
}

package io.smallrye.graphql.schema.schemadirectives;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@RepeatableSchemaDirective(name = "name2")
@RepeatableSchemaDirective(name = "name4")
public class Schema2 {
    @Query
    public String foo() {
        return "bar";
    }
}

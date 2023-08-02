package io.smallrye.graphql.schema.schemadirectives;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@RepeatableSchemaDirective(name = "name1")
public class Schema1 {
    @Query
    public String foo() {
        return "bar";
    }
}

package io.smallrye.graphql.schema.schemadirectives;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@NonRepeatableSchemaDirective
public class Schema4 {
    @Query
    public String foo() {
        return "bar";
    }
}

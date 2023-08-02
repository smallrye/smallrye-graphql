package io.smallrye.graphql.schema.schemadirectives;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@RepeatableSchemaDirective(name = "name3")
@NonRepeatableSchemaDirective
public class Schema3 {
    @Query
    public String foo() {
        return "bar";
    }
}

package io.smallrye.graphql.schema.directiveswithenumvalues;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class SomeApi {
    @Query
    public MyObject getMyObject() {
        return new MyObject("Test");
    }
}

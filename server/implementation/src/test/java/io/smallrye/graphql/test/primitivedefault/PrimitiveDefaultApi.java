package io.smallrye.graphql.test.primitivedefault;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class PrimitiveDefaultApi {

    @Query
    public int intDefault(@DefaultValue("5") int i) {
        return i;
    }
}

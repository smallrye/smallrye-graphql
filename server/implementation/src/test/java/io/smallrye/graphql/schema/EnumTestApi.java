package io.smallrye.graphql.schema;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class EnumTestApi {

    @Query
    public int query(EnumWithDirectives enumWithDirectives) {
        return 0;
    }

    @EnumDirective
    @Description("EnumWithDirectives description")
    public enum EnumWithDirectives {
        @EnumDirective
        @Description("A description")
        A,
        B
    }

}

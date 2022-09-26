package io.smallrye.graphql.schema;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class InputTestApi {

    @Query
    public int query(InputWithDirectives input) {
        return 0;
    }

    @InputDirective
    public static class InputWithDirectives {
        @InputDirective
        public int foo;

        @InputDirective
        public void setBar(int bar) {
        }
    }

}

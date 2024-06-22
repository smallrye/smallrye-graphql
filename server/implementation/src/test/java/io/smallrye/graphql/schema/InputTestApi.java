package io.smallrye.graphql.schema;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class InputTestApi {

    @Query
    public int query(InputWithDirectives input) {
        return 0;
    }

    @InputDirective
    @Description("InputType description")
    public static class InputWithDirectives {
        @InputDirective
        @Description("InputTypeField description")
        public int foo;

        @InputDirective
        public void setBar(int bar) {
        }
    }
}

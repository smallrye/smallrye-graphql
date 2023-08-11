package io.smallrye.graphql.schema;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.schema.schemadirectives.OutputDirective;

@GraphQLApi
public class InputTestApi {

    @Query
    public int query(InputWithDirectives input) {
        return 0;
    }

    @Query
    public int someQuery(SomeObject someObject) {
        return 1;
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

    @OutputDirective
    public static class SomeObject {
        int boo;

        public void setBoo(int boo) {
            this.boo = boo;
        }
    }

}

package io.smallrye.graphql.schema;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Union;

@GraphQLApi
public class UnionTestApi {

    @Query
    public SomeUnion query() {
        return null;
    }

    static class SomeClass implements SomeUnion {
        private int number;

        public int getNumber() {
            return number;
        }

    }

    @Union
    @UnionDirective(value = "C")
    @UnionDirective(value = "A")
    @InputDirective // should be ignored
    @UnionDirective(value = "B")
    interface SomeUnion {
    }

}

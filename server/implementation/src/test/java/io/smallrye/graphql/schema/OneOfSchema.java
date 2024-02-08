package io.smallrye.graphql.schema;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.OneOf;

@GraphQLApi
public class OneOfSchema {
    @OneOf
    static class SomeClass {
        Integer field;

        public SomeClass() {
        }

        public Integer getField() {
            return field;
        }

        public void setField(Integer field) {
            this.field = field;
        }
    }

    @Query
    public SomeClass someQuery(SomeClass someClass) {
        return null;
    }
}

package io.smallrye.graphql.schema;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
public class SourceFieldTestApi {

    @Query
    public SomeType query() {
        return null;
    }

    // non-batch source method
    @OperationDirective
    public SomeOtherType operation(@Source SomeType someType) {
        return null;
    }

    // batch source method
    @OperationDirective
    public List<SomeOtherType> batchOperation(@Source List<SomeType> someTypes) {
        return null;
    }

    static class SomeType {
    }

    static class SomeOtherType {

        private String string;

        public String getString() {
            return string;
        }

        public void setString(String string) {
            this.string = string;
        }
    }

}

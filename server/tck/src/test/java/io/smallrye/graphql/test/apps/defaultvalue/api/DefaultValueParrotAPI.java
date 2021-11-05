package io.smallrye.graphql.test.apps.defaultvalue.api;

import java.util.List;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;

/**
 * Testing Default values
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@GraphQLApi
public class DefaultValueParrotAPI {

    @Query
    public String[] arrayDefault(@DefaultValue("[\"creature\",\"comfort\"]") String[] values) {
        return values;
    }

    @Query
    public List<String> listDefault(@DefaultValue("[\"electric\",\"blue\"]") List<String> values) {
        return values;
    }

    @Query
    public Root objectFieldDefaults(@NonNull Root input) {
        return input;
    }

    @Input
    public static class Root {
        @DefaultValue("[\"dancing\",\"shepard\"]")
        public String[] stringArray;

        @DefaultValue("[\"poignant\",\"joker\"]")
        public List<String> stringList;

        @DefaultValue("[{\"field\": \"angry\"}, {\"field\": \"jack\"}]")
        public Nested[] nestedArray;

        @DefaultValue("[{\"field\": \"big\"}, {\"field\": \"grunt\"}]")
        public List<Nested> nestedList;
    }

    @Input
    public static class Nested {
        public String field;
    }
}

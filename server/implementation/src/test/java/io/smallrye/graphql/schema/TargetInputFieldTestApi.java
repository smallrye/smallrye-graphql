package io.smallrye.graphql.schema;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Target;

@GraphQLApi
public class TargetInputFieldTestApi {

    @Query
    public String ping() {
        return "pong";
    }

    @Mutation
    public TargetType save(TargetType input) {
        return input;
    }

    @Name("renamed")
    @Description("Target field description")
    public void apply(@Target TargetType target, Integer value) {
    }

    public static class TargetType {
        public String title;
    }
}

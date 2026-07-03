package io.smallrye.graphql.test.targetinput;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.api.Target;

@GraphQLApi
public class TargetApi {
    @Query
    public String ping() {
        return "pong";
    }

    @Mutation
    public TargetInput save(@Name("input") TargetInput input) {
        return input;
    }

    public void applyValue(@Target TargetInput input, String value) {
        input.value = "target:" + input.title + ":" + value;
    }

    public void applyNumber(@Target TargetInput input, Integer number) {
        input.value = "number:" + number;
    }

    public void applyWithContext(@Target TargetInput input, String value, Context context) {
        input.value = "context:" + context.getFieldName() + ":" + value;
    }
}

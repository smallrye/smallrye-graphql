package io.smallrye.graphql.index.duplicates.targetinput.targetfield;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;

import io.smallrye.graphql.api.Target;
import io.smallrye.graphql.index.duplicates.targetinput.SomeInput;

@GraphQLApi
public class ApiWithTargetFieldNameDuplicates {

    public void password(@Target SomeInput someInput, String password) {
    }

    @Mutation
    public SomeInput save(SomeInput input) {
        return input;
    }
}

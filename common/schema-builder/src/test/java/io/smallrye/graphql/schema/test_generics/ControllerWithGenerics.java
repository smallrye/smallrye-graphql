package io.smallrye.graphql.schema.test_generics;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class ControllerWithGenerics {

    @Query
    public String getString() {
        return null;
    }

    @Query
    public ClassWithoutGenerics getClassWithoutGenerics() {
        return null;
    }

    @Query
    public ClassWithOneGenericsParam<String> getClassWithOneGenericsParamInControllerString() {
        return null;
    }

    @Query
    public ClassWithTwoGenericsParams<String, ClassWithOneGenericsParam<Integer>> getClassWithTwoGenericsParamsWithNestedOneGenericsParamInControllerString() {
        return null;
    }

    @Query
    public InterfaceWithOneGenericsParam<String> getInterfaceWithOneGenericsParamInControllerString() {
        return null;
    }

    @Query
    public InterfaceWithOneGenericsParam<Integer> getInterfaceWithOneGenericsParamInControllerInteger() {
        return null;
    }

    @Query
    public ClassWithOneGenericsParamToString2 getClassWithOneGenericsParamToString() {
        return null;
    }

    @Mutation
    public InterfaceWithOneGenericsParam<String> setClassWithOneGenericsParamInControllerString(
            ClassWithOneGenericsParam<String> param1) {
        return null;
    }
}

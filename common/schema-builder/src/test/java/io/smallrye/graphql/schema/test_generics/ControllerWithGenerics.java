package io.smallrye.graphql.schema.test_generics;

import java.util.Date;
import java.util.List;

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
    public List<ClassWithOneGenericsParam<String>> getListOfClassWithOneGenericsParamInControllerString() {
        return null;
    }

    @Query
    public ClassWithOneGenericsParam<String>[] getArrayOfClassWithOneGenericsParamInControllerString() {
        return null;
    }

    @Query
    public ClassWithOneGenericsParam<Integer> getClassWithOneGenericsParamInControllerInteger() {
        return null;
    }

    @Query
    public ClassWithOneGenericsParam<Date> getClassWithOneGenericsParamInControllerDate() {
        return null;
    }

    @Query
    public ClassWithOneGenericsParam<ClassWithoutGenericsWithNameAnnotation> getClassWithOneGenericsParamInControllerClassWithoutGenericsWithNameAnnotation() {
        return null;
    }

    @Query
    public ClassWithOneGenericsParamWithNameAnnotation<Integer> getClassWithOneGenericsParamWithNameAnnotation() {
        return null;
    }

    @Query
    public ClassWithTwoGenericsParams<String, ClassWithOneGenericsParam<Integer>> getClassWithTwoGenericsParamsWithNestedOneGenericsParamInControllerString() {
        return null;
    }

    @Query
    public ClassWithTwoGenericsParams<String, ClassWithOneGenericsParam<Integer>>[] getArrayOfClassWithTwoGenericsParamsWithNestedOneGenericsParamInControllerString() {
        return null;
    }

    @Query
    public List<ClassWithTwoGenericsParams<String, ClassWithOneGenericsParam<Integer>>> getListOfClassWithTwoGenericsParamsWithNestedOneGenericsParamInControllerString() {
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
    public InterfaceWithOneGenericsParam<Date> getInterfaceWithOneGenericsParamInControllerDate() {
        return null;
    }

    @Query
    public InterfaceWithOneGenericsParam<ClassWithoutGenericsWithNameAnnotation> getInterfaceWithOneGenericsParamInControllerClassWithoutGenericsWithNameAnnotation() {
        return null;
    }

    @Query
    public ClassWithOneGenericsParamToString2 getClassWithOneGenericsParamToString() {
        return null;
    }

    // error #423 replicator
    @Query
    public ClassWithGenericAttributeResolvedFromEnclosingClass<String> getClassWithGenericAttributeResolvedFromEnclosingClass() {
        return null;
    }

    // error #423 replicator
    @Query
    public ClassWithGenericListAttributeResolvedFromEnclosingClass<Date> getClassWithGenericListAttributeResolvedFromEnclosingClass() {
        return null;
    }

    // error #423 replicator
    @Query
    public ClassWithGenericArrayAttributeResolvedFromEnclosingClass<Integer> getClassWithGenericArrayAttributeResolvedFromEnclosingClass() {
        return null;
    }

    @Query
    public ClassWithOneGenericsParamFromInterface<Long> getClassWithOneGenericsParamFromInterface() {
        return null;
    }

    @Query
    public List<ClassWithOneGenericsParamFromInterface<Long>> getListOfClassWithOneGenericsParamFromInterface() {
        return null;
    }

    @Query
    public ClassWithOneGenericsParamFromInterface<Long>[] getArrayOfClassWithOneGenericsParamFromInterface() {
        return null;
    }

    //error #??? reproducer
    @Query
    public ClassFromInterfaceWithOneGenericsListParam getClassFromInterfaceWithOneGenericsListParam() {
        return null;
    }

    @Mutation
    public InterfaceWithOneGenericsParam<String> setClassWithOneGenericsParamInControllerStringReturnInterface(
            ClassWithOneGenericsParam<String> param1) {
        return null;
    }

    @Mutation
    public ClassWithOneGenericsParam<String> setClassWithOneGenericsParamInControllerString(
            ClassWithOneGenericsParam<String> param1) {
        return null;
    }

    @Mutation
    public ClassWithOneGenericsParam<String>[] setArrayOfClassWithOneGenericsParamInControllerString(
            ClassWithOneGenericsParam<String> param1) {
        return null;
    }

    @Mutation
    public ClassWithGenericArrayAttributeResolvedFromEnclosingClass<Integer> setClassWithGenericArrayAttributeResolvedFromEnclosingClass(
            ClassWithGenericArrayAttributeResolvedFromEnclosingClass<Integer> param1) {
        return null;
    }

    @Mutation
    public InterfaceWithOneGenericsParam<Integer> setClassWithOneGenericsParamInControllerIntegerReturnInterface(
            ClassWithOneGenericsParam<Integer> param1) {
        return null;
    }
}

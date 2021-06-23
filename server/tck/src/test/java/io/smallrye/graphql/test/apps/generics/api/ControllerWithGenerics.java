package io.smallrye.graphql.test.apps.generics.api;

import static java.time.ZoneOffset.UTC;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class ControllerWithGenerics {

    private final Date date = Date.from(LocalDate.of(2020, 1, 1).atStartOfDay(UTC).toInstant());

    @Query
    public String getString() {
        return "String";
    }

    @Query
    public ClassWithoutGenerics getClassWithoutGenerics() {
        return new ClassWithoutGenerics();
    }

    @Query
    public ClassWithOneGenericsParam<String> getClassWithOneGenericsParamInControllerString() {
        return new ClassWithOneGenericsParam<>("Hello");
    }

    @Query
    public List<ClassWithOneGenericsParam<String>> getListOfClassWithOneGenericsParamInControllerString() {
        List<ClassWithOneGenericsParam<String>> s = new ArrayList<>();
        s.add(new ClassWithOneGenericsParam<>("Hello"));
        return s;
    }

    @Query
    public ClassWithOneGenericsParam<String>[] getArrayOfClassWithOneGenericsParamInControllerString() {
        List<ClassWithOneGenericsParam<String>> s = new ArrayList<>();
        s.add(new ClassWithOneGenericsParam<>("Hello"));
        return s.toArray(new ClassWithOneGenericsParam[] {});
    }

    @Query
    public ClassWithOneGenericsParam<Integer> getClassWithOneGenericsParamInControllerInteger() {
        return new ClassWithOneGenericsParam<>(1);
    }

    @Query
    public ClassWithOneGenericsParam<Date> getClassWithOneGenericsParamInControllerDate() {
        return new ClassWithOneGenericsParam<>(date);
    }

    @Query
    public ClassWithOneGenericsParam<ClassWithoutGenericsWithNameAnnotation> getClassWithOneGenericsParamInControllerClassWithoutGenericsWithNameAnnotation() {
        ClassWithoutGenericsWithNameAnnotation classWithoutGenericsWithNameAnnotation = new ClassWithoutGenericsWithNameAnnotation();
        return new ClassWithOneGenericsParam<>(classWithoutGenericsWithNameAnnotation);
    }

    @Query
    public ClassWithOneGenericsParamWithNameAnnotation<Integer> getClassWithOneGenericsParamWithNameAnnotation() {
        return new ClassWithOneGenericsParamWithNameAnnotation(2);
    }

    @Query
    public ClassWithTwoGenericsParams<String, ClassWithOneGenericsParam<Integer>> getClassWithTwoGenericsParamsWithNestedOneGenericsParamInControllerString() {
        return new ClassWithTwoGenericsParams<>("param1", new ClassWithOneGenericsParam<>(3));
    }

    @Query
    public List<ClassWithTwoGenericsParams<String, ClassWithOneGenericsParam<Integer>>> getListOfClassWithTwoGenericsParamsWithNestedOneGenericsParamInControllerString() {
        List<ClassWithTwoGenericsParams<String, ClassWithOneGenericsParam<Integer>>> s = new ArrayList<>();
        s.add(new ClassWithTwoGenericsParams<>("Hello", new ClassWithOneGenericsParam<>(4)));
        return s;
    }

    @Query
    public ClassWithTwoGenericsParams<String, ClassWithOneGenericsParam<Integer>>[] getArrayOfClassWithTwoGenericsParamsWithNestedOneGenericsParamInControllerString() {
        List<ClassWithTwoGenericsParams<String, ClassWithOneGenericsParam<Integer>>> s = new ArrayList<>();
        s.add(new ClassWithTwoGenericsParams<>("Hello", new ClassWithOneGenericsParam<>(4)));
        return s.toArray(new ClassWithTwoGenericsParams[] {});
    }

    @Query
    public InterfaceWithOneGenericsParam<String> getInterfaceWithOneGenericsParamInControllerString() {
        return new ClassWithOneGenericsParamFromInterface("Hello");
    }

    @Query
    public InterfaceWithOneGenericsParam<Integer> getInterfaceWithOneGenericsParamInControllerInteger() {
        return new ClassWithOneGenericsParamFromInterface(5);
    }

    @Query
    public InterfaceWithOneGenericsParam<Date> getInterfaceWithOneGenericsParamInControllerDate() {
        return new ClassWithOneGenericsParamFromInterface(date);
    }

    @Query
    public InterfaceWithOneGenericsParam<ClassWithoutGenericsWithNameAnnotation> getInterfaceWithOneGenericsParamInControllerClassWithoutGenericsWithNameAnnotation() {
        return new ClassWithOneGenericsParamFromInterface(new ClassWithoutGenericsWithNameAnnotation());
    }

    @Query
    public ClassWithOneGenericsParamToString2 getClassWithOneGenericsParamToString() {
        return new ClassWithOneGenericsParamToString2("Goodbye");
    }

    @Query
    public ClassWithGenericAttributeResolvedFromEnclosingClass<String> getClassWithGenericAttributeResolvedFromEnclosingClass() {
        return new ClassWithGenericAttributeResolvedFromEnclosingClass("Boo");
    }

    @Query
    public ClassWithGenericListAttributeResolvedFromEnclosingClass<Date> getClassWithGenericListAttributeResolvedFromEnclosingClass() {
        return new ClassWithGenericListAttributeResolvedFromEnclosingClass(date);
    }

    @Query
    public ClassWithGenericArrayAttributeResolvedFromEnclosingClass<Integer> getClassWithGenericArrayAttributeResolvedFromEnclosingClass() {
        return new ClassWithGenericArrayAttributeResolvedFromEnclosingClass(8);
    }

    @Query
    public ClassWithOneGenericsParamFromInterface<Long> getClassWithOneGenericsParamFromInterface() {
        return new ClassWithOneGenericsParamFromInterface(9L);
    }

    @Query
    public List<ClassWithOneGenericsParamFromInterface<Long>> getListOfClassWithOneGenericsParamFromInterface() {
        List<ClassWithOneGenericsParamFromInterface<Long>> l = new ArrayList<>();
        l.add(new ClassWithOneGenericsParamFromInterface(10L));
        return l;
    }

    @Query
    public ClassWithOneGenericsParamFromInterface<Long>[] getArrayOfClassWithOneGenericsParamFromInterface() {
        List<ClassWithOneGenericsParamFromInterface<Long>> l = new ArrayList<>();
        l.add(new ClassWithOneGenericsParamFromInterface(10L));
        return l.toArray(new ClassWithOneGenericsParamFromInterface[] {});
    }

    @Query
    public ClassFromInterfaceWithOneGenericsListParam getClassFromInterfaceWithOneGenericsListParam() {
        return new ClassFromInterfaceWithOneGenericsListParam(new ClassWithoutGenerics());
    }

    @Mutation
    public InterfaceWithOneGenericsParam<String> setClassWithOneGenericsParamInControllerStringReturnInterface(
            ClassWithOneGenericsParam<String> param1) {
        return new ClassWithOneGenericsParamFromInterface(param1.getParam1());
    }

    @Mutation
    public ClassWithOneGenericsParam<String> setClassWithOneGenericsParamInControllerString(
            ClassWithOneGenericsParam<String> param1) {
        return new ClassWithOneGenericsParam<>(param1.getParam1());
    }

    @Mutation
    public ClassWithGenericArrayAttributeResolvedFromEnclosingClass<Integer> setClassWithGenericArrayAttributeResolvedFromEnclosingClass(
            ClassWithGenericArrayAttributeResolvedFromEnclosingClass<Integer> param1) {
        return new ClassWithGenericArrayAttributeResolvedFromEnclosingClass(param1.getParam1()[0].getParam1());
    }

    @Mutation
    public ClassWithOneGenericsParam<String>[] setArrayOfClassWithOneGenericsParamInControllerString(
            ClassWithOneGenericsParam<String> param1) {
        List<ClassWithOneGenericsParam<String>> s = new ArrayList<>();
        s.add(new ClassWithOneGenericsParam<>(param1.getParam1()));
        return s.toArray(new ClassWithOneGenericsParam[] {});
    }

    @Mutation
    public InterfaceWithOneGenericsParam<Integer> setClassWithOneGenericsParamInControllerIntegerReturnInterface(
            ClassWithOneGenericsParam<Integer> param1) {
        return new ClassWithOneGenericsParamFromInterface(param1.getParam1());
    }

    @Mutation
    public ClassWithOneGenericsParam<LocalDate> setClassWithOneGenericsParamInControllerLocalDate(
            ClassWithOneGenericsParam<LocalDate> param1) {
        return new ClassWithOneGenericsParam<>(param1.getParam1());
    }
}

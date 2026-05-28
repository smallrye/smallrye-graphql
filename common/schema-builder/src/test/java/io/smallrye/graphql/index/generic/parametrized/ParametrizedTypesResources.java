package io.smallrye.graphql.index.generic.parametrized;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Type;

@GraphQLApi
public class ParametrizedTypesResources {

    static class FirstClass extends SecondClass<Bar> {
        int firstClassField;

        public FirstClass() {
        }

        public int getFirstClassField() {
            return firstClassField;
        }

        public void setFirstClassField(int firstClassField) {
            this.firstClassField = firstClassField;
        }
    }

    static class SecondClass<T> {
        T secondClassField;

        public SecondClass() {
        }

        public T getSecondClassField() {
            return secondClassField;
        }

        public void setSecondClassField(T secondClassField) {
            this.secondClassField = secondClassField;
        }
    }

    // this is the only exception where it works to use both type variable in child and parent classes.
    // first that the both `T` are the same types and that the SecondClass<T> also uses "T" in its class definition.
    static class ThirdClass<T> extends SecondClass<T> {
        T thirdClassField;

        public ThirdClass() {
        }

        public T getThirdClassField() {
            return thirdClassField;
        }

        public void setThirdClassField(T thirdClassField) {
            this.thirdClassField = thirdClassField;
        }
    }

    static class FourthClass extends FifthClass<Bar, String> {
        String fourthClassField;

        public FourthClass() {
        }

        public String getFourthClassField() {
            return fourthClassField;
        }

        public void setFourthClassField(String fourthClassField) {
            this.fourthClassField = fourthClassField;
        }
    }

    static class FifthClass<M, N> {
        M fifthClassField1;
        N fifthClassField2;

        public FifthClass() {
        }

        public M getFifthClassField1() {
            return fifthClassField1;
        }

        public void setFifthClassField1(M fifthClassField1) {
            this.fifthClassField1 = fifthClassField1;
        }

        public N getFifthClassField2() {
            return fifthClassField2;
        }

        public void setFifthClassField2(N fifthClassField2) {
            this.fifthClassField2 = fifthClassField2;
        }
    }

    static class Bar {
        int barField;

        public Bar() {
        }

        public int getBarField() {
            return barField;
        }

        public void setBarField(int barField) {
            this.barField = barField;
        }
    }

    @Type("NewFirstClass")
    @Input("NewFirstClassInput")
    static class RenamedFirstClass extends SecondClass<Bar> {
        float renamedFirstClassField;

        public RenamedFirstClass() {
        }

        public float getRenamedFirstClassField() {
            return renamedFirstClassField;
        }

        public void setRenamedFirstClassField(float renamedFirstClassField) {
            this.renamedFirstClassField = renamedFirstClassField;
        }
    }

    @Type("NewSecondClass")
    @Input("NewSecondClassInput")
    static class RenamedSecondClass<T> {
        T renamedSecondClassField;

        public RenamedSecondClass() {
        }

        public T getRenamedSecondClassField() {
            return renamedSecondClassField;
        }

        public void setRenamedSecondClassField(T renamedSecondClassField) {
            this.renamedSecondClassField = renamedSecondClassField;
        }
    }

    @Query
    public FirstClass firstQuery(FirstClass firstClass) {
        return null;
    }

    @Query
    public SecondClass<Bar> secondQuery(SecondClass<Bar> secondClass) {
        return null;
    }

    @Query
    public ThirdClass<String> thirdQuery(ThirdClass<String> thirdClass) {
        return null;
    }

    @Query
    public FourthClass fourthQuery(FourthClass fourthClass) {
        return null;
    }

    @Query
    public FifthClass<Bar, String> fifthQuery(FifthClass<String, Bar> fifthClass) {
        return null;
    }

    @Query
    public RenamedFirstClass sixthQuery(RenamedFirstClass renamedFirstClass) {
        return null;
    }

    @Query
    public RenamedSecondClass<Bar> seventhQuery(RenamedSecondClass<Bar> renamedSecondClass) {
        return null;
    }

    static class GenericBase<A> {
        A baseField;
        A[] baseArrayField;

        public GenericBase() {
        }

        public A getBaseField() {
            return baseField;
        }

        public void setBaseField(A baseField) {
            this.baseField = baseField;
        }

        public A[] getBaseArrayField() {
            return baseArrayField;
        }

        public void setBaseArrayField(A[] baseArrayField) {
            this.baseArrayField = baseArrayField;
        }
    }

    static class GenericMiddle<B> extends GenericBase<B> {

        public GenericMiddle() {
        }
    }

    static class GenericSpecialized<C extends Number> extends GenericMiddle<C> {

        public GenericSpecialized() {
        }
    }

    static class GenericLeaf extends GenericSpecialized<Integer> {

        public GenericLeaf() {
        }
    }

    static class GenericPair<X, Y> {
        X first;
        Y second;

        public GenericPair() {
        }

        public X getFirst() {
            return first;
        }

        public void setFirst(X first) {
            this.first = first;
        }

        public Y getSecond() {
            return second;
        }

        public void setSecond(Y second) {
            this.second = second;
        }
    }

    static class HalfConcretePair<T> extends GenericPair<T, String> {

        public HalfConcretePair() {
        }
    }

    static class FullyConcretePair extends HalfConcretePair<Bar> {

        public FullyConcretePair() {
        }
    }

    static class GenericRoot<R> {
        R rootField;

        public GenericRoot() {
        }

        public R getRootField() {
            return rootField;
        }

        public void setRootField(R rootField) {
            this.rootField = rootField;
        }
    }

    static class NonGenericMiddle extends GenericRoot<String> {

        public NonGenericMiddle() {
        }
    }

    static class ConcreteFromNonGeneric extends NonGenericMiddle {
        int concreteField;

        public ConcreteFromNonGeneric() {
        }

        public int getConcreteField() {
            return concreteField;
        }

        public void setConcreteField(int concreteField) {
            this.concreteField = concreteField;
        }
    }

    static class BaseWithT<T> {
        T baseValue;

        public BaseWithT() {
        }

        public T getBaseValue() {
            return baseValue;
        }

        public void setBaseValue(T baseValue) {
            this.baseValue = baseValue;
        }
    }

    static class MiddleWithT<T> extends BaseWithT<Integer> {
        T middleValue;
        T[] middleArrayValue;

        public MiddleWithT() {
        }

        public T getMiddleValue() {
            return middleValue;
        }

        public void setMiddleValue(T middleValue) {
            this.middleValue = middleValue;
        }

        public T[] getMiddleArrayValue() {
            return middleArrayValue;
        }

        public void setMiddleArrayValue(T[] middleArrayValue) {
            this.middleArrayValue = middleArrayValue;
        }
    }

    static class LeafFromMiddleWithT extends MiddleWithT<String> {

        public LeafFromMiddleWithT() {
        }
    }

    @Query
    public GenericLeaf genericLeafQuery(GenericLeaf input) {
        return null;
    }

    @Query
    public GenericMiddle<String> genericMiddleQuery(GenericMiddle<String> input) {
        return null;
    }

    @Query
    public FullyConcretePair fullyConcretePairQuery(FullyConcretePair input) {
        return null;
    }

    @Query
    public ConcreteFromNonGeneric concreteFromNonGenericQuery(ConcreteFromNonGeneric input) {
        return null;
    }

    @Query
    public LeafFromMiddleWithT leafFromMiddleWithTQuery(LeafFromMiddleWithT input) {
        return null;
    }

    static class NestedCollections {
        List<Integer> singleList;
        List<List<Integer>> doubleList;
        List<List<List<Integer>>> tripleList;

        public NestedCollections() {
        }

        public List<Integer> getSingleList() {
            return singleList;
        }

        public void setSingleList(List<Integer> singleList) {
            this.singleList = singleList;
        }

        public List<List<Integer>> getDoubleList() {
            return doubleList;
        }

        public void setDoubleList(List<List<Integer>> doubleList) {
            this.doubleList = doubleList;
        }

        public List<List<List<Integer>>> getTripleList() {
            return tripleList;
        }

        public void setTripleList(List<List<List<Integer>>> tripleList) {
            this.tripleList = tripleList;
        }
    }

    @Query
    public NestedCollections nestedCollectionsQuery(NestedCollections input) {
        return null;
    }

}

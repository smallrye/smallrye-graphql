package io.smallrye.graphql.index.generic.parametrized;

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

}

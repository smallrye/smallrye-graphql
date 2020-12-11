package com.github.t1.annotations.tck;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.t1.annotations.Stereotype;
import com.github.t1.annotations.tck.MixinClasses.AnotherAnnotation;

public class StereotypeClasses {
    @Retention(RUNTIME)
    @Target(ANNOTATION_TYPE)
    // i.e. not on TYPE
    public @interface SomeMetaAnnotation {
    }

    @Stereotype
    @Retention(RUNTIME)
    @AnotherAnnotation
    @SomeAnnotation("from-stereotype")
    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    @SomeMetaAnnotation
    public @interface SomeStereotype {
    }

    @Stereotype
    @Retention(RUNTIME)
    @SomeAnnotation("from-another-stereotype")
    public @interface AnotherStereotype {
    }

    @Stereotype
    @Retention(RUNTIME)
    @RepeatableAnnotation(1)
    public @interface StereotypeWithOne {
    }

    @Stereotype
    @Retention(RUNTIME)
    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    public @interface StereotypeWithTwo {
    }

    @Stereotype
    @Retention(RUNTIME)
    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    @RepeatableAnnotation(3)
    public @interface StereotypeWithThree {
    }

    @Stereotype
    @Retention(RUNTIME)
    @RepeatableAnnotation(11)
    public @interface AnotherStereotypeWithOne {
    }

    @Stereotype
    @Retention(RUNTIME)
    @RepeatableAnnotation(11)
    @RepeatableAnnotation(12)
    public @interface AnotherStereotypeWithTwo {
    }

    @Stereotype
    @Retention(RUNTIME)
    @SomeStereotype
    public @interface SomeIndirectedStereotype {
    }

    @Stereotype
    @Retention(RUNTIME)
    @SomeStereotype
    public @interface SomeTardyIndirectedStereotype {
    }

    @Stereotype
    @Retention(RUNTIME)
    @SomeIndirectedStereotype
    public @interface SomeDoubleIndirectedStereotype {
    }

    @SomeStereotype
    @RepeatableAnnotation(5)
    public static class StereotypedClass {
    }

    @SomeStereotype
    @SomeAnnotation("on-class")
    public static class StereotypedClassWithSomeAnnotation {
    }

    @SomeIndirectedStereotype
    public static class IndirectlyStereotypedClass {
    }

    @SomeTardyIndirectedStereotype
    public static class TardyIndirectlyStereotypedClass {
    }

    @SomeDoubleIndirectedStereotype
    public static class DoubleIndirectlyStereotypedClass {
    }

    @SomeStereotype
    @AnotherStereotype
    @RepeatableAnnotation(6)
    public static class DoubleStereotypedClass {
    }

    @StereotypeWithOne
    @AnotherStereotypeWithOne
    public static class MergeRepeatableAnnotationFromOneAndOne {
    }

    @StereotypeWithOne
    @AnotherStereotypeWithTwo
    public static class MergeRepeatableAnnotationFromOneAndTwo {
    }

    @StereotypeWithTwo
    @AnotherStereotypeWithOne
    public static class MergeRepeatableAnnotationFromTwoAndOne {
    }

    @StereotypeWithTwo
    @AnotherStereotypeWithTwo
    public static class MergeRepeatableAnnotationFromTwoAndTwo {
    }

    @StereotypeWithOne
    @RepeatableAnnotation(2)
    public static class MergeOneRepeatableAnnotationIntoOne {
    }

    @StereotypeWithOne
    @RepeatableAnnotation(21)
    @RepeatableAnnotation(22)
    public static class MergeOneRepeatableAnnotationIntoTwo {
    }

    @StereotypeWithTwo
    @RepeatableAnnotation(21)
    public static class MergeTwoRepeatableAnnotationIntoOne {
    }

    @StereotypeWithTwo
    @RepeatableAnnotation(21)
    @RepeatableAnnotation(22)
    public static class MergeTwoRepeatableAnnotationIntoTwo {
    }

    @StereotypeWithThree
    @RepeatableAnnotation(21)
    @RepeatableAnnotation(22)
    @RepeatableAnnotation(23)
    public static class MergeThreeRepeatableAnnotationIntoThree {
    }

    @SuppressWarnings("unused")
    public static class ClassWithStereotypedField {
        @SomeStereotype
        @SomeAnnotation("on-field")
        @RepeatableAnnotation(7)
        String foo;
        boolean bar;
    }

    @SuppressWarnings("unused")
    public static class ClassWithStereotypedMethod {
        @SomeStereotype
        @SomeAnnotation("on-method")
        @RepeatableAnnotation(7)
        String foo() {
            return "foo";
        }

        String bar() {
            return "bar";
        }
    }
}

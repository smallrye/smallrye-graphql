package com.github.t1.annotations.tck;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.t1.annotations.Stereotype;

public class StereotypeClasses {
    @Retention(RUNTIME)
    @Target(ANNOTATION_TYPE) // i.e. not on TYPE
    public @interface SomeMetaAnnotation {
    }

    @Stereotype
    @Retention(RUNTIME)
    @SomeAnnotation("some-stereotype")
    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    @SomeMetaAnnotation
    public @interface SomeStereotype {
    }

    @Stereotype
    @Retention(RUNTIME)
    @SomeAnnotation("another-stereotype")
    @RepeatableAnnotation(3)
    @RepeatableAnnotation(4)
    public @interface AnotherStereotype {
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

    @SuppressWarnings("unused")
    public static class ClassWithStereotypedField {
        @SomeStereotype
        @RepeatableAnnotation(7)
        String foo;
        boolean bar;
    }

    @SuppressWarnings("unused")
    public static class ClassWithStereotypedMethod {
        @SomeStereotype
        @RepeatableAnnotation(7)
        String foo() {
            return "foo";
        }

        String bar() {
            return "bar";
        }
    }
}

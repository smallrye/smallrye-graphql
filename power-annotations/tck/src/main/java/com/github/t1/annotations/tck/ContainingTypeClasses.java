package com.github.t1.annotations.tck;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

public class ContainingTypeClasses {

    @Retention(RUNTIME)
    public @interface SomeAnnotationWithoutTargetAnnotation {
    }

    @Retention(RUNTIME)
    @Target(TYPE)
    public @interface SomeAnnotationWithOnlyTypeTargetAnnotation {
    }

    @SomeAnnotation("class-annotation")
    @SomeAnnotationWithoutTargetAnnotation
    @SomeAnnotationWithOnlyTypeTargetAnnotation
    public static class ClassWithField {
        @SuppressWarnings("unused")
        String someField;
    }

    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    public static class ClassWithRepeatedAnnotationsForField {
        @SuppressWarnings("unused")
        String someField;
    }

    @RepeatableAnnotation(2)
    public static class ClassWithRepeatableAnnotationOnClassAndField {
        @RepeatableAnnotation(1)
        @SuppressWarnings("unused")
        String someField;
    }

    @SomeAnnotation("class-annotation")
    public static class ClassWithAnnotationsOnClassAndField {
        @SuppressWarnings("unused")
        @RepeatableAnnotation(1)
        String someField;
    }

    @SomeAnnotation("class-annotation")
    @SomeAnnotationWithoutTargetAnnotation
    @SomeAnnotationWithOnlyTypeTargetAnnotation
    public static class ClassWithMethod {
        @SuppressWarnings("unused")
        void someMethod() {
        }
    }

    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    public static class ClassWithRepeatedAnnotationsForMethod {
        @SuppressWarnings("unused")
        void someMethod() {
        }
    }

    @RepeatableAnnotation(2)
    public static class ClassWithRepeatableAnnotationOnClassAndMethod {
        @RepeatableAnnotation(1)
        @SuppressWarnings("unused")
        void someMethod() {
        }
    }

    @SomeAnnotation("class-annotation")
    public static class ClassWithAnnotationsOnClassAndMethod {
        @SuppressWarnings("unused")
        @RepeatableAnnotation(1)
        void someMethod() {
        }
    }
}

package com.github.t1.annotations.tck;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import com.github.t1.annotations.tck.MixinClasses.AnotherAnnotation;

public class DirectAnnotationClasses {
    @Retention(RUNTIME)
    public @interface SomeAnnotationWithDefaultValue {
        String valueWithDefault() default "default-value";
    }

    public static class SomeUnannotatedClass {
    }

    @SomeAnnotation("class-annotation")
    public static class SomeAnnotatedClass {
    }

    @SomeAnnotation("interface-annotation")
    @SomeAnnotationWithDefaultValue
    public interface SomeAnnotatedInterface {
    }

    public static class SomeClassWithUnannotatedField {
        @SuppressWarnings("unused")
        String foo;
    }

    public static class SomeClassWithAnnotatedField {
        @SuppressWarnings("unused")
        @SomeAnnotation("field-annotation")
        private String foo;
    }

    public static class SomeClassWithUnannotatedMethod {
        @SuppressWarnings("unused")
        void foo(String x) {
        }
    }

    public static class SomeClassWithAnnotatedMethod {
        @SuppressWarnings("unused")
        @SomeAnnotation("method-annotation")
        void foo(String x) {
        }
    }

    public interface SomeInterfaceWithAnnotatedMethod {
        @SomeAnnotation("method-annotation")
        void foo(@AnotherAnnotation String x);
    }
}

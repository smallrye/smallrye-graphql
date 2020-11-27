package com.github.t1.annotations.tck;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import com.github.t1.annotations.Stereotype;

public class CombinedAnnotationClasses {
    @Stereotype
    @SomeAnnotation("from-stereotype")
    @Retention(RUNTIME)
    public @interface SomeStereotype {
    }

    @SomeStereotype
    public interface SomeStereotypedInterface {
        @SuppressWarnings("unused")
        void foo();
    }

    @SomeStereotype
    public static class SomeStereotypedClass {
        @SuppressWarnings("unused")
        public void foo() {
        }
    }

    @SomeAnnotation("from-sub-interface")
    public interface SomeInheritingInterface extends SomeInheritedInterface {
    }

    public interface SomeInheritedInterface {
        @SuppressWarnings("unused")
        void foo();
    }
}

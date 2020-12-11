package com.github.t1.annotations.tck;

public class RepeatableAnnotationClasses {
    @RepeatableAnnotation(1)
    public static class UnrepeatedAnnotationClass {
        @RepeatableAnnotation(10)
        public String foo;

        @RepeatableAnnotation(20)
        public void bar() {
        }
    }

    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    public static class RepeatedAnnotationClass {
        @RepeatableAnnotation(11)
        @RepeatableAnnotation(12)
        public String foo;

        @RepeatableAnnotation(21)
        @RepeatableAnnotation(22)
        public void bar() {
        }
    }
}

package com.github.t1.annotations.tck;

public class RepeatableAnnotationClasses {
    @RepeatableAnnotation(1)
    public static class UnrepeatedAnnotationClass {
    }

    @RepeatableAnnotation(1)
    @RepeatableAnnotation(2)
    public static class RepeatedAnnotationClass {
    }
}

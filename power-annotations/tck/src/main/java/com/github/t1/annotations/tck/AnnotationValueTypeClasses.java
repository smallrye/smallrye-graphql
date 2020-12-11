package com.github.t1.annotations.tck;

import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnnotationValueTypeClasses {
    @Retention(RUNTIME)
    public @interface DifferentValueTypesAnnotation {
        boolean booleanValue() default false;

        byte byteValue() default 0;

        char charValue() default 0;

        short shortValue() default 0;

        int intValue() default 0;

        long longValue() default 0;

        float floatValue() default 0;

        double doubleValue() default 0;

        String stringValue() default "";

        RetentionPolicy enumValue() default SOURCE;

        Class<?> classValue() default Void.class;

        SomeAnnotation annotationValue() default @SomeAnnotation("");

        boolean[] booleanArrayValue() default false;

        byte[] byteArrayValue() default 0;

        char[] charArrayValue() default 0;

        short[] shortArrayValue() default 0;

        int[] intArrayValue() default 0;

        long[] longArrayValue() default 0;

        double[] doubleArrayValue() default 0;

        float[] floatArrayValue() default 0;

        String[] stringArrayValue() default "";

        RetentionPolicy[] enumArrayValue() default SOURCE;

        Class<?>[] classArrayValue() default Void.class;

        SomeAnnotation[] annotationArrayValue() default @SomeAnnotation("");
    }

    @DifferentValueTypesAnnotation(booleanValue = true)
    public static class AnnotatedWithBooleanValueClass {
    }

    @DifferentValueTypesAnnotation(byteValue = 1)
    public static class AnnotatedWithByteValueClass {
    }

    @DifferentValueTypesAnnotation(charValue = 'a')
    public static class AnnotatedWithCharValueClass {
    }

    @DifferentValueTypesAnnotation(shortValue = 1234)
    public static class AnnotatedWithShortValueClass {
    }

    @DifferentValueTypesAnnotation(intValue = 42)
    public static class AnnotatedWithIntValueClass {
    }

    @DifferentValueTypesAnnotation(longValue = 44L)
    public static class AnnotatedWithLongValueClass {
    }

    @DifferentValueTypesAnnotation(floatValue = 1.2F)
    public static class AnnotatedWithFloatValueClass {
    }

    @DifferentValueTypesAnnotation(doubleValue = 12.34D)
    public static class AnnotatedWithDoubleValueClass {
    }

    @DifferentValueTypesAnnotation(stringValue = "foo")
    public static class AnnotatedWithStringValueClass {
    }

    @DifferentValueTypesAnnotation(enumValue = RUNTIME)
    public static class AnnotatedWithEnumValueClass {
    }

    @DifferentValueTypesAnnotation(classValue = String.class)
    public static class AnnotatedWithClassValueClass {
    }

    @DifferentValueTypesAnnotation(annotationValue = @SomeAnnotation("annotation-value"))
    public static class AnnotatedWithAnnotationValueClass {
    }

    @DifferentValueTypesAnnotation(booleanArrayValue = { true, false })
    public static class AnnotatedWithBooleanArrayValueClass {
    }

    @DifferentValueTypesAnnotation(byteArrayValue = { 1, 2 })
    public static class AnnotatedWithByteArrayValueClass {
    }

    @DifferentValueTypesAnnotation(charArrayValue = { 'a', 'b' })
    public static class AnnotatedWithCharArrayValueClass {
    }

    @DifferentValueTypesAnnotation(shortArrayValue = { 1234, 1235 })
    public static class AnnotatedWithShortArrayValueClass {
    }

    @DifferentValueTypesAnnotation(intArrayValue = { 42, 43 })
    public static class AnnotatedWithIntArrayValueClass {
    }

    @DifferentValueTypesAnnotation(longArrayValue = { 44L, 45L })
    public static class AnnotatedWithLongArrayValueClass {
    }

    @DifferentValueTypesAnnotation(floatArrayValue = { 1.2F, 1.3F })
    public static class AnnotatedWithFloatArrayValueClass {
    }

    @DifferentValueTypesAnnotation(doubleArrayValue = { 12.34D, 12.35D })
    public static class AnnotatedWithDoubleArrayValueClass {
    }

    @DifferentValueTypesAnnotation(stringArrayValue = { "foo", "bar" })
    public static class AnnotatedWithStringArrayValueClass {
    }

    @DifferentValueTypesAnnotation(enumArrayValue = { RUNTIME, CLASS })
    public static class AnnotatedWithEnumArrayValueClass {
    }

    @DifferentValueTypesAnnotation(classArrayValue = { String.class, Integer.class })
    public static class AnnotatedWithClassArrayValueClass {
    }

    @DifferentValueTypesAnnotation(annotationArrayValue = { @SomeAnnotation("annotation-value1"),
            @SomeAnnotation("annotation-value2") })
    public static class AnnotatedWithAnnotationArrayValueClass {
    }
}

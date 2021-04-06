package test;

import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithAnnotationArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithAnnotationValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithBooleanArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithBooleanValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithByteArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithByteValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithCharArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithCharValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithClassArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithClassValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithDoubleArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithDoubleValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithEnumArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithEnumValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithFloatArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithFloatValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithIntArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithIntValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithLongArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithLongValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithShortArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithShortValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithStringArrayValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.AnnotatedWithStringValueClass;
import com.github.t1.annotations.tck.AnnotationValueTypeClasses.DifferentValueTypesAnnotation;
import com.github.t1.annotations.tck.SomeAnnotation;

class AnnotationValueTypeBehavior {

    @Test
    void shouldGetBooleanAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithBooleanValueClass.class);

        then(annotation.booleanValue()).isTrue();
    }

    @Test
    void shouldGetByteAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithByteValueClass.class);

        then(annotation.byteValue()).isEqualTo((byte) 1);
    }

    @Test
    void shouldGetCharAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithCharValueClass.class);

        then(annotation.charValue()).isEqualTo('a');
    }

    @Test
    void shouldGetShortAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithShortValueClass.class);

        then(annotation.shortValue()).isEqualTo((short) 1234);
    }

    @Test
    void shouldGetIntAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithIntValueClass.class);

        then(annotation.intValue()).isEqualTo(42);
    }

    @Test
    void shouldGetLongAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithLongValueClass.class);

        then(annotation.longValue()).isEqualTo(44L);
    }

    @Test
    void shouldGetFloatAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithFloatValueClass.class);

        then(annotation.floatValue()).isEqualTo(1.2F);
    }

    @Test
    void shouldGetDoubleAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithDoubleValueClass.class);

        then(annotation.doubleValue()).isEqualTo(12.34);
    }

    @Test
    void shouldGetStringAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithStringValueClass.class);

        then(annotation.stringValue()).isEqualTo("foo");
    }

    @Test
    void shouldGetEnumAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithEnumValueClass.class);

        then(annotation.enumValue()).isEqualTo(RUNTIME);
    }

    @Test
    void shouldGetClassAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithClassValueClass.class);

        then(annotation.classValue()).isEqualTo(String.class);
    }

    @Test
    void shouldGetAnnotationAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithAnnotationValueClass.class);

        then(annotation.annotationValue().value()).isEqualTo("annotation-value");
    }

    @Test
    void shouldGetBooleanArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithBooleanArrayValueClass.class);

        then(annotation.booleanArrayValue()).contains(true, false);
    }

    @Test
    void shouldGetByteArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithByteArrayValueClass.class);

        then(annotation.byteArrayValue()).containsExactly((byte) 1, (byte) 2);
    }

    @Test
    void shouldGetCharArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithCharArrayValueClass.class);

        then(annotation.charArrayValue()).containsExactly('a', 'b');
    }

    @Test
    void shouldGetShortArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithShortArrayValueClass.class);

        then(annotation.shortArrayValue()).containsExactly((short) 1234, (short) 1235);
    }

    @Test
    void shouldGetIntArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithIntArrayValueClass.class);

        then(annotation.intArrayValue()).containsExactly(42, 43);
    }

    @Test
    void shouldGetLongArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithLongArrayValueClass.class);

        then(annotation.longArrayValue()).containsExactly(44L, 45L);
    }

    @Test
    void shouldGetFloatArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithFloatArrayValueClass.class);

        then(annotation.floatArrayValue()).containsExactly(1.2F, 1.3F);
    }

    @Test
    void shouldGetDoubleArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithDoubleArrayValueClass.class);

        then(annotation.doubleArrayValue()).containsExactly(12.34, 12.35);
    }

    @Test
    void shouldGetStringArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithStringArrayValueClass.class);

        then(annotation.stringArrayValue()).containsExactly("foo", "bar");
    }

    @Test
    void shouldGetEnumArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithEnumArrayValueClass.class);

        then(annotation.enumArrayValue()).containsExactly(RUNTIME, CLASS);
    }

    @Test
    void shouldGetClassArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(AnnotatedWithClassArrayValueClass.class);

        then(annotation.classArrayValue()).containsExactly(String.class, Integer.class);
    }

    @Test
    void shouldGetAnnotationArrayAnnotationValue() {
        DifferentValueTypesAnnotation annotation = getDifferentValueTypesAnnotation(
                AnnotatedWithAnnotationArrayValueClass.class);

        then(Stream.of(annotation.annotationArrayValue()).map(SomeAnnotation::value))
                .containsExactly("annotation-value1", "annotation-value2");
    }

    private DifferentValueTypesAnnotation getDifferentValueTypesAnnotation(Class<?> type) {
        Annotations annotations = Annotations.on(type);

        Optional<DifferentValueTypesAnnotation> annotation = annotations.get(DifferentValueTypesAnnotation.class);

        assert annotation.isPresent();
        DifferentValueTypesAnnotation someAnnotation = annotation.get();
        then(someAnnotation.annotationType()).isEqualTo(DifferentValueTypesAnnotation.class);
        return someAnnotation;
    }
}

package test;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.tck.DirectAnnotationClasses.SomeAnnotatedClass;
import com.github.t1.annotations.tck.DirectAnnotationClasses.SomeAnnotatedInterface;
import com.github.t1.annotations.tck.DirectAnnotationClasses.SomeAnnotationWithDefaultValue;
import com.github.t1.annotations.tck.DirectAnnotationClasses.SomeClassWithAnnotatedField;
import com.github.t1.annotations.tck.DirectAnnotationClasses.SomeClassWithAnnotatedMethod;
import com.github.t1.annotations.tck.DirectAnnotationClasses.SomeClassWithUnannotatedField;
import com.github.t1.annotations.tck.DirectAnnotationClasses.SomeClassWithUnannotatedMethod;
import com.github.t1.annotations.tck.DirectAnnotationClasses.SomeInterfaceWithAnnotatedMethod;
import com.github.t1.annotations.tck.DirectAnnotationClasses.SomeUnannotatedClass;
import com.github.t1.annotations.tck.MixinClasses.AnotherAnnotation;
import com.github.t1.annotations.tck.SomeAnnotation;

public class DirectAnnotationsBehavior {

    @Nested
    class ClassAnnotations {
        @Test
        void shouldGetNoClassAnnotation() {
            Annotations annotations = Annotations.on(SomeUnannotatedClass.class);

            thenEmpty(annotations);
        }

        @Test
        void shouldGetClassAnnotation() {
            Annotations annotations = Annotations.on(SomeAnnotatedClass.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "class-annotation");
        }

        @Test
        void shouldGetInterfaceAnnotation() {
            Annotations annotations = Annotations.on(SomeAnnotatedInterface.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "interface-annotation");
        }

        @Test
        void shouldGetDefaultValueOfClassAnnotation() {
            Annotations annotations = Annotations.on(SomeAnnotatedInterface.class);

            Optional<SomeAnnotationWithDefaultValue> annotation = annotations.get(SomeAnnotationWithDefaultValue.class);

            assert annotation.isPresent();
            SomeAnnotationWithDefaultValue someAnnotation = annotation.get();
            then(someAnnotation.annotationType()).isEqualTo(SomeAnnotationWithDefaultValue.class);
            then(someAnnotation.valueWithDefault()).isEqualTo("default-value");
            then(someAnnotation).isNotSameAs(SomeAnnotatedInterface.class.getAnnotation(SomeAnnotation.class));
        }

        @Test
        void shouldGetAllTypeAnnotations() {
            Annotations annotations = Annotations.on(SomeAnnotatedInterface.class);

            Stream<Annotation> list = annotations.all();

            then(list.map(Object::toString)).containsExactlyInAnyOrder(
                    "@" + SomeAnnotationWithDefaultValue.class.getName(),
                    "@" + SomeAnnotation.class.getName() + "(value = \"interface-annotation\")");
        }
    }

    @Nested
    class FieldAnnotations {
        @Test
        void shouldGetNoFieldAnnotation() {
            Annotations annotations = Annotations.onField(SomeClassWithUnannotatedField.class, "foo");

            thenEmpty(annotations);
        }

        @Test
        void shouldFailToGetUnknownFieldAnnotation() {
            Throwable throwable = catchThrowable(() -> Annotations.onField(SomeClassWithAnnotatedField.class, "bar"));

            then(throwable)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("no field 'bar' in " + SomeClassWithAnnotatedField.class); // implementation detail?
        }

        @Test
        void shouldGetFieldAnnotation() {
            Annotations annotations = Annotations.onField(SomeClassWithAnnotatedField.class, "foo");

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "field-annotation");
        }
    }

    @Nested
    class MethodAnnotations {
        @Test
        void shouldGetNoMethodAnnotation() {
            Annotations annotations = Annotations.onMethod(SomeClassWithUnannotatedMethod.class, "foo", String.class);

            thenEmpty(annotations);
        }

        @Test
        void shouldGetMethodAnnotation() {
            Annotations annotations = Annotations.onMethod(SomeClassWithAnnotatedMethod.class, "foo", String.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "method-annotation");
        }

        @Test
        void shouldGetInterfaceMethodAnnotation() {
            Annotations annotations = Annotations.onMethod(SomeInterfaceWithAnnotatedMethod.class, "foo", String.class);

            Optional<SomeAnnotation> annotation = annotations.get(SomeAnnotation.class);

            thenIsSomeAnnotation(annotation, "method-annotation");
        }

        @Test
        void shouldGetAllMethodAnnotations() throws NoSuchMethodException {
            Annotations annotations = Annotations.onMethod(SomeInterfaceWithAnnotatedMethod.class, "foo", String.class);

            Stream<Annotation> all = annotations.all();

            // the parameter annotation must be there, but not represented as method annotation
            then(fooMethod().getParameterAnnotations()[0][0].toString())
                    .startsWith("@" + AnotherAnnotation.class.getName()); // `since` and `forRemoval` are JDK 9+
            then(all.map(Object::toString)).containsExactlyInAnyOrder(
                    "@" + SomeAnnotation.class.getName() + "(value = \"method-annotation\")");
        }

        private Method fooMethod() throws NoSuchMethodException {
            return SomeInterfaceWithAnnotatedMethod.class.getDeclaredMethod("foo", String.class);
        }

        @Test
        void shouldFailToGetAnnotationsFromUnknownMethodName() {
            Throwable throwable = catchThrowable(
                    () -> Annotations.onMethod(SomeClassWithAnnotatedMethod.class, "bar", String.class));

            then(throwable)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("no method bar(java.lang.String) in " + SomeClassWithAnnotatedMethod.class); // implementation detail?
        }

        @Test
        void shouldFailToGetAnnotationsFromMethodWithTooManyArguments() {
            Throwable throwable = catchThrowable(() -> Annotations.onMethod(SomeClassWithAnnotatedMethod.class, "foo"));

            then(throwable)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("no method foo() in " + SomeClassWithAnnotatedMethod.class); // implementation detail?
        }

        @Test
        void shouldFailToGetAnnotationsFromMethodWithTooFewArguments() {
            Throwable throwable = catchThrowable(
                    () -> Annotations.onMethod(SomeClassWithAnnotatedMethod.class, "foo", String.class, int.class));

            then(throwable)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("no method foo(java.lang.String, int) in " + SomeClassWithAnnotatedMethod.class); // implementation detail?
        }

        @Test
        void shouldFailToGetAnnotationsFromMethodWithWrongArgumentType() {
            Throwable throwable = catchThrowable(
                    () -> Annotations.onMethod(SomeClassWithAnnotatedMethod.class, "foo", int.class));

            then(throwable)
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("no method foo(int) in " + SomeClassWithAnnotatedMethod.class); // implementation detail?
        }
    }

    void thenEmpty(Annotations annotations) {
        then(annotations.all()).isEmpty();
        then(annotations.get(SomeAnnotation.class)).isEmpty();
    }

    void thenIsSomeAnnotation(
            @SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<SomeAnnotation> annotation,
            String expectedValue) {
        assert annotation.isPresent();
        SomeAnnotation someAnnotation = annotation.get();
        then(someAnnotation.annotationType()).isEqualTo(SomeAnnotation.class);
        then(someAnnotation.value()).isEqualTo(expectedValue);
    }
}

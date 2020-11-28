package test;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.t1.annotations.AmbiguousAnnotationResolutionException;
import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.tck.RepeatableAnnotation;
import com.github.t1.annotations.tck.RepeatableAnnotationClasses.RepeatedAnnotationClass;
import com.github.t1.annotations.tck.RepeatableAnnotationClasses.UnrepeatedAnnotationClass;

/**
 * Actually tests how the Annotations utils resolve repeatable annotations, not the power annotations implementation,
 * but this is a prerequisite for many other tests.
 */
public class RepeatableAnnotationBehavior {

    @Test
    void shouldGetSingleRepeatedClassAnnotation() {
        Annotations annotations = Annotations.on(UnrepeatedAnnotationClass.class);

        Optional<RepeatableAnnotation> annotation = annotations.get(RepeatableAnnotation.class);

        assert annotation.isPresent();
        then(annotation.get().value()).isEqualTo(1);
    }

    @Test
    void shouldGetSingleRepeatedFieldAnnotation() {
        Annotations annotations = Annotations.onField(UnrepeatedAnnotationClass.class, "foo");

        Optional<RepeatableAnnotation> annotation = annotations.get(RepeatableAnnotation.class);

        assert annotation.isPresent();
        then(annotation.get().value()).isEqualTo(10);
    }

    @Test
    void shouldGetSingleRepeatedMethodAnnotation() {
        Annotations annotations = Annotations.onMethod(UnrepeatedAnnotationClass.class, "bar");

        Optional<RepeatableAnnotation> annotation = annotations.get(RepeatableAnnotation.class);

        assert annotation.isPresent();
        then(annotation.get().value()).isEqualTo(20);
    }

    @Nested
    class RepeatedClassAnnotationBehavior {
        Annotations repeatedAnnotations = Annotations.on(RepeatedAnnotationClass.class);

        @Test
        void shouldFailToGetRepeatingAnnotation() {
            Throwable throwable = catchThrowable(() -> repeatedAnnotations.get(RepeatableAnnotation.class));

            then(throwable)
                    .isInstanceOf(AmbiguousAnnotationResolutionException.class)
                    // TODO message detail about the target .hasMessageContaining(SomeClass.class.getName())
                    .hasMessageContaining(RepeatableAnnotation.class.getName());
        }

        @Test
        void shouldGetAll() {
            Stream<Annotation> all = repeatedAnnotations.all();

            then(all.map(Object::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
        }

        @Test
        void shouldGetTypedAll() {
            Stream<RepeatableAnnotation> all = repeatedAnnotations.all(RepeatableAnnotation.class);

            then(all.map(RepeatableAnnotation::value)).containsExactlyInAnyOrder(1, 2);
        }
    }

    @Nested
    class RepeatedFieldAnnotationBehavior {
        Annotations repeatedAnnotations = Annotations.onField(RepeatedAnnotationClass.class, "foo");

        @Test
        void shouldFailToGetRepeatingAnnotation() {
            Throwable throwable = catchThrowable(() -> repeatedAnnotations.get(RepeatableAnnotation.class));

            then(throwable)
                    .isInstanceOf(AmbiguousAnnotationResolutionException.class)
                    // TODO message detail about the target .hasMessageContaining(SomeClass.class.getName())
                    .hasMessageContaining(RepeatableAnnotation.class.getName());
        }

        @Test
        void shouldGetAll() {
            Stream<Annotation> all = repeatedAnnotations.all();

            then(all.map(Object::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 11)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 12)");
        }

        @Test
        void shouldGetTypedAll() {
            Stream<RepeatableAnnotation> all = repeatedAnnotations.all(RepeatableAnnotation.class);

            then(all.map(RepeatableAnnotation::value)).containsExactlyInAnyOrder(11, 12);
        }
    }

    @Nested
    class RepeatedMethodAnnotationBehavior {
        Annotations repeatedAnnotations = Annotations.onMethod(RepeatedAnnotationClass.class, "bar");

        @Test
        void shouldFailToGetRepeatingAnnotation() {
            Throwable throwable = catchThrowable(() -> repeatedAnnotations.get(RepeatableAnnotation.class));

            then(throwable)
                    .isInstanceOf(AmbiguousAnnotationResolutionException.class)
                    // TODO message detail about the target .hasMessageContaining(SomeClass.class.getName())
                    .hasMessageContaining(RepeatableAnnotation.class.getName());
        }

        @Test
        void shouldGetAll() {
            Stream<Annotation> all = repeatedAnnotations.all();

            then(all.map(Object::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 21)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 22)");
        }

        @Test
        void shouldGetTypedAll() {
            Stream<RepeatableAnnotation> all = repeatedAnnotations.all(RepeatableAnnotation.class);

            then(all.map(RepeatableAnnotation::value)).containsExactlyInAnyOrder(21, 22);
        }
    }
}

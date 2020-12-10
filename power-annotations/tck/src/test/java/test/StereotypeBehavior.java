package test;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.BDDAssertions.then;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.tck.MixinClasses.AnotherAnnotation;
import com.github.t1.annotations.tck.RepeatableAnnotation;
import com.github.t1.annotations.tck.SomeAnnotation;
import com.github.t1.annotations.tck.StereotypeClasses.AnotherStereotype;
import com.github.t1.annotations.tck.StereotypeClasses.ClassWithStereotypedField;
import com.github.t1.annotations.tck.StereotypeClasses.ClassWithStereotypedMethod;
import com.github.t1.annotations.tck.StereotypeClasses.DoubleIndirectlyStereotypedClass;
import com.github.t1.annotations.tck.StereotypeClasses.DoubleStereotypedClass;
import com.github.t1.annotations.tck.StereotypeClasses.IndirectlyStereotypedClass;
import com.github.t1.annotations.tck.StereotypeClasses.MergeOneRepeatableAnnotationIntoOne;
import com.github.t1.annotations.tck.StereotypeClasses.MergeOneRepeatableAnnotationIntoTwo;
import com.github.t1.annotations.tck.StereotypeClasses.MergeRepeatableAnnotationFromOneAndOne;
import com.github.t1.annotations.tck.StereotypeClasses.MergeRepeatableAnnotationFromOneAndTwo;
import com.github.t1.annotations.tck.StereotypeClasses.MergeRepeatableAnnotationFromTwoAndOne;
import com.github.t1.annotations.tck.StereotypeClasses.MergeRepeatableAnnotationFromTwoAndTwo;
import com.github.t1.annotations.tck.StereotypeClasses.MergeThreeRepeatableAnnotationIntoThree;
import com.github.t1.annotations.tck.StereotypeClasses.MergeTwoRepeatableAnnotationIntoOne;
import com.github.t1.annotations.tck.StereotypeClasses.MergeTwoRepeatableAnnotationIntoTwo;
import com.github.t1.annotations.tck.StereotypeClasses.SomeDoubleIndirectedStereotype;
import com.github.t1.annotations.tck.StereotypeClasses.SomeIndirectedStereotype;
import com.github.t1.annotations.tck.StereotypeClasses.SomeStereotype;
import com.github.t1.annotations.tck.StereotypeClasses.SomeTardyIndirectedStereotype;
import com.github.t1.annotations.tck.StereotypeClasses.StereotypedClass;
import com.github.t1.annotations.tck.StereotypeClasses.StereotypedClassWithSomeAnnotation;
import com.github.t1.annotations.tck.StereotypeClasses.TardyIndirectlyStereotypedClass;

public class StereotypeBehavior {

    @Nested
    class StereotypedClasses {
        Annotations annotations = Annotations.on(StereotypedClass.class);

        @Test
        void shouldGetAnnotationFromClassStereotype() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("from-stereotype");
        }

        @Test
        void shouldGetAllAnnotationsFromClassStereotype() {
            Stream<Annotation> someAnnotation = annotations.all();

            then(someAnnotation.map(Object::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 5)",
                    "@" + AnotherAnnotation.class.getName(),
                    "@" + SomeStereotype.class.getName(),
                    "@" + SomeAnnotation.class.getName() + "(value = \"from-stereotype\")",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
        }

        @Test
        void shouldGetAllNonRepeatableAnnotationsFromClassStereotype() {
            Stream<SomeAnnotation> someAnnotation = annotations.all(SomeAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + SomeAnnotation.class.getName() + "(value = \"from-stereotype\")");
        }

        @Test
        void shouldGetAllRepeatableAnnotationFromClassStereotype() {
            Stream<RepeatableAnnotation> someAnnotation = annotations.all(RepeatableAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 5)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
        }

        @Test
        void shouldGetAllFromIndirectClassStereotype() {
            Annotations annotations = Annotations.on(IndirectlyStereotypedClass.class);

            Stream<Annotation> all = annotations.all();

            then(all.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + SomeAnnotation.class.getName() + "(value = \"from-stereotype\")",
                    "@" + AnotherAnnotation.class.getName(),
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                    "@" + SomeStereotype.class.getName(),
                    "@" + SomeIndirectedStereotype.class.getName());
        }

        @Test
        void shouldGetAllFromIndirectClassStereotypeResolvedAlphabeticallyAfterSomeStereotype() {
            Annotations annotations = Annotations.on(TardyIndirectlyStereotypedClass.class);

            Stream<Annotation> all = annotations.all();

            then(all.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + SomeAnnotation.class.getName() + "(value = \"from-stereotype\")",
                    "@" + AnotherAnnotation.class.getName(),
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                    "@" + SomeStereotype.class.getName(),
                    "@" + SomeTardyIndirectedStereotype.class.getName());
        }

        @Test
        void shouldGetAllFromDoubleIndirectClassStereotype() {
            Annotations annotations = Annotations.on(DoubleIndirectlyStereotypedClass.class);

            Stream<Annotation> all = annotations.all();

            then(all.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + SomeAnnotation.class.getName() + "(value = \"from-stereotype\")",
                    "@" + AnotherAnnotation.class.getName(),
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                    "@" + SomeStereotype.class.getName(),
                    "@" + SomeIndirectedStereotype.class.getName(),
                    "@" + SomeDoubleIndirectedStereotype.class.getName());
        }

        @Test
        void shouldNotReplaceClassAnnotationWithStereotypedNonRepeatableAnnotation() {
            Annotations annotations = Annotations.on(StereotypedClassWithSomeAnnotation.class);

            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("on-class");
        }
    }

    @Nested
    class DoubleStereotypedClasses {
        Annotations annotations = Annotations.on(DoubleStereotypedClass.class);

        @Test
        void shouldGetFirstOfAmbiguousAnnotationFromTwoStereotypes() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isIn( // both are allowed:
                    "from-stereotype",
                    "from-another-stereotype");
        }

        @Test
        void shouldGetAllNonRepeatableAnnotationsFromTwoStereotypes() {
            Stream<SomeAnnotation> someAnnotations = annotations.all(SomeAnnotation.class);

            then(someAnnotations.map(Objects::toString)).containsAnyOf( // both are allowed:
                    "@" + SomeAnnotation.class.getName() + "(value = \"from-stereotype\")",
                    "@" + SomeAnnotation.class.getName() + "(value = \"from-another-stereotype\")");
        }
    }

    @Nested
    class StereotypeMerging {
        @Test
        void shouldMergeRepeatableAnnotationFromOneAndOne() {
            Annotations annotations = Annotations.on(MergeRepeatableAnnotationFromOneAndOne.class);

            Stream<RepeatableAnnotation> repeatableAnnotations = annotations.all(RepeatableAnnotation.class);

            then(repeatableAnnotations.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 11)");
        }

        @Test
        void shouldMergeRepeatableAnnotationFromOneAndTwo() {
            Annotations annotations = Annotations.on(MergeRepeatableAnnotationFromOneAndTwo.class);

            Stream<RepeatableAnnotation> repeatableAnnotations = annotations.all(RepeatableAnnotation.class);

            then(repeatableAnnotations.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 11)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 12)");
        }

        @Test
        void shouldMergeRepeatableAnnotationFromTwoAndOne() {
            Annotations annotations = Annotations.on(MergeRepeatableAnnotationFromTwoAndOne.class);

            Stream<RepeatableAnnotation> repeatableAnnotations = annotations.all(RepeatableAnnotation.class);

            then(repeatableAnnotations.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 11)");
        }

        @Test
        void shouldMergeRepeatableAnnotationFromTwoAndTwo() {
            Annotations annotations = Annotations.on(MergeRepeatableAnnotationFromTwoAndTwo.class);

            Stream<RepeatableAnnotation> repeatableAnnotations = annotations.all(RepeatableAnnotation.class);

            then(repeatableAnnotations.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 11)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 12)");
        }

        @Test
        void shouldMergeOneRepeatableAnnotationIntoOne() {
            Annotations annotations = Annotations.on(MergeOneRepeatableAnnotationIntoOne.class);

            Stream<RepeatableAnnotation> repeatableAnnotations = annotations.all(RepeatableAnnotation.class);

            then(repeatableAnnotations.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
        }

        @Test
        void shouldMergeOneRepeatableAnnotationIntoTwo() {
            Annotations annotations = Annotations.on(MergeOneRepeatableAnnotationIntoTwo.class);

            Stream<RepeatableAnnotation> repeatableAnnotations = annotations.all(RepeatableAnnotation.class);

            then(repeatableAnnotations.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 21)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 22)");
        }

        @Test
        void shouldMergeTwoRepeatableAnnotationIntoOne() {
            Annotations annotations = Annotations.on(MergeTwoRepeatableAnnotationIntoOne.class);

            Stream<RepeatableAnnotation> repeatableAnnotations = annotations.all(RepeatableAnnotation.class);

            then(repeatableAnnotations.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 21)");
        }

        @Test
        void shouldMergeTwoRepeatableAnnotationIntoTwo() {
            Annotations annotations = Annotations.on(MergeTwoRepeatableAnnotationIntoTwo.class);

            Stream<RepeatableAnnotation> repeatableAnnotations = annotations.all(RepeatableAnnotation.class);

            then(repeatableAnnotations.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 21)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 22)");
        }

        @Test
        void shouldMergeThreeRepeatableAnnotationIntoThree() {
            Annotations annotations = Annotations.on(MergeThreeRepeatableAnnotationIntoThree.class);

            Stream<RepeatableAnnotation> repeatableAnnotations = annotations.all(RepeatableAnnotation.class);

            then(repeatableAnnotations.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 3)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 21)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 22)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 23)");
        }

        @Test
        void shouldGetAllAnnotationsFromTwoStereotypes() {
            Annotations annotations = Annotations.on(DoubleStereotypedClass.class);

            Stream<Annotation> all = annotations.all();

            List<String> list = all.map(Objects::toString).collect(toList());
            then(list).containsExactlyInAnyOrder(
                    "@" + SomeStereotype.class.getName(),
                    "@" + AnotherStereotype.class.getName(),
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 6)",
                    "@" + AnotherAnnotation.class.getName(),
                    "@" + SomeAnnotation.class.getName() + "(value = \"from-another-stereotype\")");
        }
    }

    @Nested
    class StereotypedFields {
        Annotations annotations = Annotations.onField(ClassWithStereotypedField.class, "foo");

        @Test
        void shouldGetAnnotationFromFieldStereotype() {
            Optional<AnotherAnnotation> someAnnotation = annotations.get(AnotherAnnotation.class);

            assert someAnnotation.isPresent();
        }

        @Test
        void shouldNotReplaceFieldAnnotationWithStereotypedNonRepeatableAnnotation() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("on-field");
        }

        @Test
        void shouldGetAllAnnotationsFromFieldStereotype() {
            Stream<Annotation> someAnnotation = annotations.all();

            then(someAnnotation.map(Object::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 7)",
                    "@" + AnotherAnnotation.class.getName(),
                    "@" + SomeStereotype.class.getName(),
                    "@" + SomeAnnotation.class.getName() + "(value = \"on-field\")",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
        }

        @Test
        void shouldGetAllAnnotationNonRepeatableTypedFromFieldStereotype() {
            Stream<SomeAnnotation> someAnnotation = annotations.all(SomeAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + SomeAnnotation.class.getName() + "(value = \"on-field\")");
        }
    }

    @Nested
    class StereotypedMethods {
        Annotations annotations = Annotations.onMethod(ClassWithStereotypedMethod.class, "foo");

        @Test
        void shouldGetAnnotationFromMethodStereotype() {
            Optional<AnotherAnnotation> someAnnotation = annotations.get(AnotherAnnotation.class);

            assert someAnnotation.isPresent();
        }

        @Test
        void shouldNotReplaceMethodAnnotationWithStereotypedNonRepeatableAnnotation() {
            Optional<SomeAnnotation> someAnnotation = annotations.get(SomeAnnotation.class);

            assert someAnnotation.isPresent();
            then(someAnnotation.get().value()).isEqualTo("on-method");
        }

        @Test
        void shouldGetAllAnnotationsFromMethodStereotype() {
            Stream<Annotation> someAnnotation = annotations.all();

            then(someAnnotation.map(Object::toString)).containsExactlyInAnyOrder(
                    "@" + RepeatableAnnotation.class.getName() + "(value = 7)",
                    "@" + AnotherAnnotation.class.getName(),
                    "@" + SomeStereotype.class.getName(),
                    "@" + SomeAnnotation.class.getName() + "(value = \"on-method\")",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                    "@" + RepeatableAnnotation.class.getName() + "(value = 2)");
        }

        @Test
        void shouldGetAllAnnotationNonRepeatableTypedFromMethodStereotype() {
            Stream<SomeAnnotation> someAnnotation = annotations.all(SomeAnnotation.class);

            then(someAnnotation.map(Objects::toString)).containsExactlyInAnyOrder(
                    "@" + SomeAnnotation.class.getName() + "(value = \"on-method\")");
        }
    }

    // TODO parameter stereotypes
}

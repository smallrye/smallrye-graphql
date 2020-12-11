package test;

import static org.assertj.core.api.BDDAssertions.then;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.github.t1.annotations.Annotations;
import com.github.t1.annotations.tck.InheritedAnnotationClasses.InheritingClass;
import com.github.t1.annotations.tck.InheritedAnnotationClasses.InheritingInterface;
import com.github.t1.annotations.tck.RepeatableAnnotation;
import com.github.t1.annotations.tck.SomeAnnotation;

@InheritedAnnotationsTestSuite
public class InheritedBehavior {

    @Test
    void shouldGetAllOnInterface() {
        Annotations annotations = Annotations.on(InheritingInterface.class);

        Stream<Annotation> all = annotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
                "@" + SomeAnnotation.class.getName() + "(value = \"1\")",
                "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 6)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 8)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 9)");
    }

    @Test
    void shouldGetAllOnInterfaceMethod() {
        Annotations annotations = Annotations.onMethod(InheritingInterface.class, "method");

        Stream<Annotation> all = annotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
                "@" + SomeAnnotation.class.getName() + "(value = \"10\")",
                "@" + RepeatableAnnotation.class.getName() + "(value = 1)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 6)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 8)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 9)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 10)");
    }

    @Test
    void shouldGetAllOnClass() {
        Annotations annotations = Annotations.on(InheritingClass.class);

        Stream<Annotation> all = annotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
                "@" + SomeAnnotation.class.getName() + "(value = \"2\")",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 3)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 6)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 8)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 9)");
    }

    @Test
    void shouldGetAllOnField() {
        Annotations annotations = Annotations.onField(InheritingClass.class, "field");

        Stream<Annotation> all = annotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
                "@" + SomeAnnotation.class.getName() + "(value = \"4\")",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 3)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 4)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 6)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 8)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 9)");
    }

    @Test
    void shouldGetAllOnClassMethod() {
        Annotations annotations = Annotations.onMethod(InheritingClass.class, "method");

        Stream<Annotation> all = annotations.all();

        then(all.map(Object::toString)).containsExactlyInAnyOrder(
                "@" + SomeAnnotation.class.getName() + "(value = \"5\")",
                "@" + RepeatableAnnotation.class.getName() + "(value = 2)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 3)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 5)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 6)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 8)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 10)",
                "@" + RepeatableAnnotation.class.getName() + "(value = 9)");
    }
}

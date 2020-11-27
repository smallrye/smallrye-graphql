package test;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.junit.jupiter.api.Tag;

@Tag("InheritedAnnotationsTestSuite")
@Retention(RUNTIME)
public @interface InheritedAnnotationsTestSuite {
}

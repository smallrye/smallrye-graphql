package com.github.t1.annotations.tck;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.github.t1.annotations.tck.RepeatableAnnotation.RepeatableAnnotations;

@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD })
@Repeatable(RepeatableAnnotations.class)
public @interface RepeatableAnnotation {
    int value();

    @Target({ TYPE, FIELD, METHOD })
    @Retention(RUNTIME)
    @interface RepeatableAnnotations {
        @SuppressWarnings("unused")
        RepeatableAnnotation[] value();
    }
}

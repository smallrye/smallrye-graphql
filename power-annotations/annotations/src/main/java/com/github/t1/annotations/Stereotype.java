package com.github.t1.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Marks an annotation to be a meta annotation, i.e. the annotations on the stereotype (i.e. the annotation that is
 * annotated as <code>Stereotype</code>) are logically copied to all targets (i.e. the classes/fields/methods that are
 * annotated with the stereotype).
 *
 * Power Annotations doesn't depend on CDI, so it handles all classes named <code>Stereotype</code> the same.
 * You can use this class if you don't have another stereotype class on your classpath,
 * but in JEE/MP applications you should have <code>javax.enterprise.inject.Stereotype</code>.
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
@Documented
public @interface Stereotype {
}

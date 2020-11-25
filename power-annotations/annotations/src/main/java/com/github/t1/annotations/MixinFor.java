package com.github.t1.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Add annotations to another class <em>that you can't change</em>; if you <em>can</em> change that class,
 * use explicit {@link Stereotype}s instead. Using Mixins is very implicit and difficult to trace back;
 * it is often better to actually copy the target class, so you can directly add your annotations,
 * instead of working with mixins. You have been warned!
 *
 * If you have a class that you don't control, e.g.:
 * 
 * <pre>
 * <code>
 *     public class ImportedClass {
 *         ...
 *     }
 * </code>
 * </pre>
 *
 * And you need it to be annotated as <code>@SomeAnnotation</code>. Then you can write a mixin (the name is arbitrary):
 * 
 * <pre>
 * <code>
 *     &#64;MixinFor(ImportedClass.class)
 *     &#64;SomeAnnotation
 *     public class ImportedClassMixin {
 *         ...
 *     }
 * </code>
 * </pre>
 *
 * After the Power Annotations have been resolved, the target class looks as if it had another annotation:
 *
 * <pre>
 * <code>
 *     &#64;SomeAnnotation
 *     public class ImportedClass {
 *         ...
 *     }
 * </code>
 * </pre>
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface MixinFor {
    Class<?> value();
}

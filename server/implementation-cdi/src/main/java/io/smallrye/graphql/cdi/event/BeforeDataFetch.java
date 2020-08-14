package io.smallrye.graphql.cdi.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * CDI Event fired before DataFetch
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE })
public @interface BeforeDataFetch {

    static BeforeDataFetch.BeforeDataFetchLiteral LITERAL = new BeforeDataFetch.BeforeDataFetchLiteral();

    class BeforeDataFetchLiteral extends AnnotationLiteral<BeforeDataFetch> implements BeforeDataFetch {

    }
}

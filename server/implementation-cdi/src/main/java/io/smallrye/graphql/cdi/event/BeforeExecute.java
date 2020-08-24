package io.smallrye.graphql.cdi.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;

/**
 * CDI Event fired before execute
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE })
public @interface BeforeExecute {

    static BeforeExecute.BeforeExecuteLiteral LITERAL = new BeforeExecute.BeforeExecuteLiteral();

    class BeforeExecuteLiteral extends AnnotationLiteral<BeforeExecute> implements BeforeExecute {

    }
}

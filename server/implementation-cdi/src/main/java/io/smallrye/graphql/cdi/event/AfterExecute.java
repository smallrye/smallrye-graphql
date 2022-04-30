package io.smallrye.graphql.cdi.event;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**
 * CDI Event fired after execute
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE })
public @interface AfterExecute {

    static AfterExecute.AfterExecuteLiteral LITERAL = new AfterExecute.AfterExecuteLiteral();

    class AfterExecuteLiteral extends AnnotationLiteral<AfterExecute> implements AfterExecute {

    }
}

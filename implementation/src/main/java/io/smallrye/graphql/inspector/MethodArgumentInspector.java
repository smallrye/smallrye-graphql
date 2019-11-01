package io.smallrye.graphql.inspector;

import java.util.Optional;

import javax.enterprise.context.Dependent;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;

/**
 * Helping to inspect Method Arguments
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class MethodArgumentInspector {

    /**
     * This method get (if it exist) a value of an annotation that is placed on a method argument
     * 
     * @param methodInfo - The method
     * @param pos - The argument position
     * @param annotation - The name of the annotation
     * @return Optional Annotation value
     */
    public Optional<AnnotationValue> getArgumentAnnotationValue(MethodInfo methodInfo, short pos, DotName annotation) {
        for (AnnotationInstance anno : methodInfo.annotations()) {
            if (anno.name().equals(annotation)) {
                if (anno.target().kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                    MethodParameterInfo methodParameter = anno.target().asMethodParameter();
                    short position = methodParameter.position();
                    if (position == pos) {
                        if (anno.value() != null) {
                            Object o = anno.value().value();
                            if (o != null) {
                                return Optional.of(anno.value());
                            }
                        }
                    }
                } else if (anno.target().kind().equals(AnnotationTarget.Kind.METHOD)) {
                    if (anno.value() != null) {
                        Object o = anno.value().value();
                        if (o != null) {
                            return Optional.of(anno.value());
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }
}

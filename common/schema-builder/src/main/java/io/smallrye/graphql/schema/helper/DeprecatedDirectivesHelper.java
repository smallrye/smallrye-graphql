package io.smallrye.graphql.schema.helper;

import java.util.Optional;
import java.util.Set;

import org.jboss.jandex.DotName;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.DirectiveType;

public class DeprecatedDirectivesHelper {

    public Optional<DirectiveInstance> transformDeprecatedToDirective(Annotations annotations, DirectiveType directiveType) {
        Set<DotName> annotationNames = annotations.getAnnotationNames();
        for (DotName annotationName : annotationNames) {
            if (annotationName.equals(DotName.createSimple("java.lang.Deprecated"))) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(directiveType);
                return Optional.of(directive);
            }
        }
        return Optional.empty();
    }
}

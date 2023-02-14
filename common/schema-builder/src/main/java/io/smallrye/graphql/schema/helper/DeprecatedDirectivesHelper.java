package io.smallrye.graphql.schema.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.DirectiveType;

public class DeprecatedDirectivesHelper {

    private static Logger LOGGER = Logger.getLogger(DeprecatedDirectivesHelper.class);

    public List<DirectiveInstance> transformDeprecatedToDirectives(Annotations annotations, DirectiveType directiveType) {
        List<DirectiveInstance> result = new ArrayList<>();
        Set<DotName> annotationNames = annotations.getAnnotationNames();
        for (DotName annotationName : annotationNames) {
            if (annotationName.equals(DotName.createSimple("java.lang.Deprecated"))) {
                DirectiveInstance directive = new DirectiveInstance();
                directive.setType(directiveType);
                result.add(directive);
            }
        }
        return result;
    }
}

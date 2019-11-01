package io.smallrye.graphql.inspector;

import java.util.Map;

import javax.enterprise.context.Dependent;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.type.TypeMappingInitializer;

/**
 * Name this better
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
public class GraphQLInstructions {
    private static final Logger LOG = Logger.getLogger(TypeMappingInitializer.class.getName());

    public boolean markAsNonNull(Type type, String name, Map<DotName, AnnotationInstance> annotations) {
        // check if the @NonNull annotation is present
        boolean hasNonNull = hasNonNull(annotations);
        // true if this is a primitive
        if (type.kind().equals(Type.Kind.PRIMITIVE)) {
            hasNonNull = true; // By implication
        }

        // check if the @DefaultValue annotation is present
        boolean hasDefaultValue = hasDefaultValue(annotations);
        if (hasDefaultValue) {
            if (hasNonNull) {
                LOG.warn("Ignoring @NonNull on [" + name + "] as there is a @DefaultValue");
            }
            return false;
        }

        return hasNonNull;
    }

    public boolean markAsNonNull(FieldInfo fieldInfo, Map<DotName, AnnotationInstance> annotations) {
        return markAsNonNull(fieldInfo.type(), fieldInfo.name(), annotations);
    }

    private boolean hasNonNull(Map<DotName, AnnotationInstance> annotations) {

        return annotations.containsKey(Annotations.NON_NULL)
                || annotations.containsKey(Annotations.BEAN_VALIDATION_NOT_NULL)
                || annotations.containsKey(Annotations.BEAN_VALIDATION_NOT_EMPTY)
                || annotations.containsKey(Annotations.BEAN_VALIDATION_NOT_BLANK);
    }

    private boolean hasDefaultValue(Map<DotName, AnnotationInstance> annotations) {
        return containsKeyAndValidValue(annotations, Annotations.DEFAULT_VALUE);
    }

    public boolean containsKeyAndValidValue(Map<DotName, AnnotationInstance> annotations, DotName key) {
        return annotations.containsKey(key) && annotations.get(key).value() != null;
    }
}

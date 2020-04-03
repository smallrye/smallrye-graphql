package io.smallrye.graphql.schema.helper;

import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;

/**
 * Helping to figure out of some should be marked as Non null
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class NonNullHelper {
    private static final Logger LOG = Logger.getLogger(NonNullHelper.class.getName());

    private NonNullHelper() {
    }

    public static boolean markAsNonNull(Type type, Annotations annotations) {
        return markAsNonNull(type, annotations, false);
    }

    public static boolean markAsNonNull(Type type, Annotations annotations, boolean ignorePrimativeCheck) {
        // check if the @NonNull annotation is present
        boolean hasNonNull = hasNonNull(annotations);
        // true if this is a primitive
        if (!ignorePrimativeCheck && type.kind().equals(Type.Kind.PRIMITIVE)) {
            hasNonNull = true; // By implication
        }

        // check if the @DefaultValue annotation is present
        boolean hasDefaultValue = hasDefaultValue(annotations);
        if (hasDefaultValue) {
            if (hasNonNull && !type.kind().equals(Type.Kind.PRIMITIVE)) {
                LOG.warn("Ignoring non null on [" + type.name() + "] as there is a @DefaultValue");
            }
            return false;
        }

        return hasNonNull;
    }

    private static boolean hasNonNull(Annotations annotations) {

        return annotations.containsOneOfTheseKeys(Annotations.NON_NULL,
                Annotations.BEAN_VALIDATION_NOT_NULL,
                Annotations.BEAN_VALIDATION_NOT_EMPTY,
                Annotations.BEAN_VALIDATION_NOT_BLANK);
    }

    private static boolean hasDefaultValue(Annotations annotations) {
        return annotations.containsKeyAndValidValue(Annotations.DEFAULT_VALUE);
    }
}

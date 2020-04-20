package io.smallrye.graphql.schema.helper;

import java.util.Optional;

import io.smallrye.graphql.schema.Annotations;

/**
 * Helping to figure out if there is a default value.
 * Looking for the @DefaultValue annotation.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DefaultValueHelper {

    /**
     * Find a default object in the annotation, or empty if nothing
     * 
     * @param annotations the annotations to search in
     * @return a optional default object
     */
    public static Optional<String> getDefaultValue(Annotations... annotations) {
        for (Annotations a : annotations) {
            if (a.containsKeyAndValidValue(Annotations.DEFAULT_VALUE)) {
                return Optional.of(a.getAnnotationValue(Annotations.DEFAULT_VALUE).value().toString());
            }
        }
        return Optional.empty();
    }

}

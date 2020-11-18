package io.smallrye.graphql.schema.helper;

import java.util.Optional;

import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;

/**
 * Helper to get the correct Description.
 * Basically looking for the @Description annotation.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DescriptionHelper {

    private DescriptionHelper() {
    }

    /**
     * Get the Description on a field or argument
     * 
     * @param annotations the annotations for that field/argument
     * @param type the java type (some types have default values)
     * @return the optional description
     */
    public static Optional<String> getDescriptionForField(Annotations annotations, Type type) {
        if (Classes.isDateLikeTypeOrContainedIn(type)) {
            String dateFormat = FormatHelper.getDateFormatString(annotations, type);
            if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
                return Optional.of(getGivenDescription(annotations) + " (" + dateFormat + ")");
            } else {
                return Optional.of(dateFormat);
            }
        } else if (Classes.isNumberLikeTypeOrContainedIn(type)) {
            Optional<String> numberFormat = FormatHelper.getNumberFormatString(annotations);
            if (numberFormat.isPresent()) {
                if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
                    return Optional.of(getGivenDescription(annotations) + " (" + numberFormat.get() + ")");
                } else {
                    return numberFormat;
                }
            }
        }

        if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
            return Optional.of(getGivenDescription(annotations));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Get the description on a class type
     * 
     * @param annotations annotation on the class
     * @return the optional description
     */
    public static Optional<String> getDescriptionForType(Annotations annotations) {
        if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
            return Optional.of(getGivenDescription(annotations));
        }
        return Optional.empty();
    }

    private static String getGivenDescription(Annotations annotations) {
        return annotations.getAnnotationValue(Annotations.DESCRIPTION).asString();
    }
}

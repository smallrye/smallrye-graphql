package io.smallrye.graphql.schema.helper;

import java.util.Optional;

import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;

/**
 * Helper to get the correct Description.
 * Basically looking for the @Description annotation.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DescriptionHelper {

    private DescriptionHelper() {
    }

    // Used by return types and input parameters, on types
    public static Optional<String> getDescriptionForField(Annotations annotations, Type type) {
        if (FormatHelper.isDateLikeTypeOrCollectionThereOf(type)) {
            String dateFormat = FormatHelper.getDateFormat(annotations, type);
            if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
                return Optional.of(getGivenDescription(annotations) + " (" + dateFormat + ")");
            } else {
                return Optional.of(dateFormat);
            }
        } else if (FormatHelper.isNumberLikeTypeOrCollectionThereOf(type)) {
            Optional<String> numberFormat = FormatHelper.getNumberFormatValue(annotations);
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

    // Used by types (Enum, Input and Output)
    public static Optional<String> getDescriptionForType(Annotations annotations) {
        if (annotations.containsKeyAndValidValue(Annotations.DESCRIPTION)) {
            return Optional.of(annotations.getAnnotationValue(Annotations.DESCRIPTION).asString());
        }
        return Optional.empty();
    }

    private static String getGivenDescription(Annotations annotations) {
        return annotations.getAnnotation(Annotations.DESCRIPTION).value().asString();
    }
}

package io.smallrye.graphql.schema.helper;

import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.model.Transformation;

/**
 * Helping with formats of dates and Numbers
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FormatHelper {

    private FormatHelper() {
    }

    /**
     * Test if any formatting is present.
     * 
     * @param annotations the annotations
     * @return if formatting is present
     */
    public static boolean hasAnyFormatting(Annotations annotations) {
        return getDateFormat(annotations).isPresent()
                || getNumberFormat(annotations).isPresent();
    }

    /**
     * Get the format model object for a certain type.
     * 
     * @param type the type
     * @param annotations the annotations
     * @return Potentially a TransformInfo model
     */
    public static Optional<Transformation> getFormat(Type type, Annotations annotations) {
        if (Classes.isDateLikeTypeOrContainedIn(type)) {
            return getDateFormat(annotations);
        } else if (Classes.isNumberLikeTypeOrContainedIn(type)) {
            return getNumberFormat(annotations);
        }
        return Optional.empty();
    }

    /**
     * The the number format (if any) as a String
     * This is used to add to the description in the schema
     * 
     * @param annotations the annotations
     * @return potentially a format as a String
     */
    public static Optional<String> getNumberFormatString(Annotations annotations) {
        Optional<AnnotationInstance> numberFormatAnnotation = getNumberFormatAnnotation(annotations);
        if (numberFormatAnnotation.isPresent()) {
            AnnotationInstance a = numberFormatAnnotation.get();
            return getFormat(a);
        }
        return Optional.empty();
    }

    /**
     * The the date format as a String
     * This is used to add to the description in the schema
     * 
     * @param annotations the annotations
     * @param type the date type
     * @return potentially a format as a String
     */
    public static String getDateFormatString(Annotations annotations, Type type) {
        Optional<String> format = Optional.empty();

        Optional<AnnotationInstance> dateFormatAnnotation = getDateFormatAnnotation(annotations);
        if (dateFormatAnnotation.isPresent()) {
            format = getFormat(dateFormatAnnotation.get());
        }

        if (format.isPresent()) {
            return format.get();
        } else {
            return ISO;
        }
    }

    private static Optional<Transformation> getNumberFormat(Annotations annotations) {
        if (annotations != null) {
            Optional<AnnotationInstance> numberFormatAnnotation = getNumberFormatAnnotation(annotations);
            if (numberFormatAnnotation.isPresent()) {
                return getNumberFormat(numberFormatAnnotation.get());
            }
        }
        return Optional.empty();
    }

    private static Optional<Transformation> getNumberFormat(AnnotationInstance annotationInstance) {
        if (annotationInstance != null) {
            String format = getStringValue(annotationInstance);
            String locale = getStringValue(annotationInstance, LOCALE);
            return Optional.of(new Transformation(
                    Transformation.Type.NUMBER,
                    format,
                    locale,
                    isJsonB(annotationInstance)));
        }
        return Optional.empty();
    }

    private static Optional<Transformation> getDateFormat(Annotations annotations) {
        if (annotations != null) {
            Optional<AnnotationInstance> dateFormatAnnotation = getDateFormatAnnotation(annotations);
            if (dateFormatAnnotation.isPresent()) {
                return getDateFormat(dateFormatAnnotation.get());
            }
        }
        return Optional.empty();
    }

    private static Optional<Transformation> getDateFormat(AnnotationInstance annotationInstance) {
        if (annotationInstance != null) {
            String format = getStringValue(annotationInstance);
            String locale = getStringValue(annotationInstance, LOCALE);
            return Optional.of(new Transformation(
                    Transformation.Type.DATE,
                    format,
                    locale,
                    isJsonB(annotationInstance)));
        }
        return Optional.empty();
    }

    private static boolean isJsonB(AnnotationInstance instance) {
        return instance.name().toString().startsWith("javax.json.bind.annotation.");
    }

    private static Optional<AnnotationInstance> getDateFormatAnnotation(Annotations annotations) {
        return annotations.getOneOfTheseAnnotations(Annotations.DATE_FORMAT, Annotations.JSONB_DATE_FORMAT);
    }

    private static Optional<AnnotationInstance> getNumberFormatAnnotation(Annotations annotations) {
        return annotations.getOneOfTheseAnnotations(Annotations.NUMBER_FORMAT, Annotations.JSONB_NUMBER_FORMAT);
    }

    private static Optional<String> getFormat(AnnotationInstance annotationInstance) {
        AnnotationValue locale = annotationInstance.value(LOCALE);
        AnnotationValue format = annotationInstance.value();

        if (format == null && locale == null) {
            return Optional.empty();
        } else if (format == null) {
            return Optional.of(locale.asString());
        } else if (locale == null) {
            return Optional.of(format.asString());
        } else {
            return Optional.of(format.asString() + " " + locale.asString());
        }
    }

    private static String getStringValue(AnnotationInstance annotationInstance) {
        return getStringValue(annotationInstance.value());
    }

    private static String getStringValue(AnnotationInstance annotationInstance, String name) {
        return getStringValue(annotationInstance.value(name));
    }

    private static String getStringValue(AnnotationValue annotationValue) {
        String value = null;
        if (annotationValue != null) {
            value = annotationValue.asString();
        }
        return value;
    }

    private static final String LOCALE = "locale";
    private static final String ISO = "ISO-8601";
}

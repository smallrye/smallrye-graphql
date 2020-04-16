package io.smallrye.graphql.schema.helper;

import java.util.Locale;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.model.TransformInfo;

/**
 * Helping with formats of dates and Numbers
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FormatHelper {

    private FormatHelper() {
    }

    /**
     * Get the format model object for a certain type.
     * 
     * @param type the type
     * @param annotations the annotations
     * @return Potentially a TransformInfo model
     */
    public static Optional<TransformInfo> getFormat(Type type, Annotations annotations) {
        if (Classes.isDateLikeTypeOrCollectionThereOf(type)) {
            return getDateFormat(type, annotations);
        } else if (Classes.isNumberLikeTypeOrCollectionThereOf(type)) {
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
            // return the default dates format
            return getDefaultDateTimeFormatString(type);
        }
    }

    private static TransformInfo getDefaultDateTimeFormat(Type type) {
        return new TransformInfo(TransformInfo.Type.DATE, getDefaultDateTimeFormatString(type), Locale.ENGLISH.toString(),
                false);
    }

    private static String getDefaultDateTimeFormatString(Type type) {
        // return the default dates format
        type = getCorrectType(type);
        if (type.name().equals(Classes.LOCALDATE) || type.name().equals(Classes.SQL_DATE)) {
            return ISO_DATE;
        } else if (type.name().equals(Classes.LOCALTIME) || type.name().equals(Classes.SQL_TIME)) {
            return ISO_TIME;
        } else if (type.name().equals(Classes.OFFSETTIME)) {
            return ISO_OFFSET_TIME;
        } else if (type.name().equals(Classes.LOCALDATETIME) || type.name().equals(Classes.SQL_TIMESTAMP)
                || type.name().equals(Classes.UTIL_DATE)) {
            return ISO_DATE_TIME;
        } else if (type.name().equals(Classes.OFFSETDATETIME)) {
            return ISO_OFFSET_DATE_TIME;
        } else if (type.name().equals(Classes.ZONEDDATETIME)) {
            return ISO_ZONED_DATE_TIME;
        }
        throw new IllegalArgumentException("Not a valid Type [" + type.name().toString() + "]");
    }

    private static Optional<TransformInfo> getNumberFormat(Annotations annotations) {
        Optional<AnnotationInstance> numberFormatAnnotation = getNumberFormatAnnotation(annotations);
        if (numberFormatAnnotation.isPresent()) {
            return getNumberFormat(numberFormatAnnotation.get());
        }
        return Optional.empty();
    }

    private static Optional<TransformInfo> getNumberFormat(AnnotationInstance annotationInstance) {
        if (annotationInstance != null) {
            String format = getStringValue(annotationInstance);
            String locale = getStringValue(annotationInstance, LOCALE);
            return Optional.of(new TransformInfo(TransformInfo.Type.NUMBER, format, locale, isJsonB(annotationInstance)));
        }
        return Optional.empty();
    }

    private static Optional<TransformInfo> getDateFormat(Type type, Annotations annotations) {
        Optional<AnnotationInstance> dateFormatAnnotation = getDateFormatAnnotation(annotations);
        if (dateFormatAnnotation.isPresent()) {
            return getDateFormat(type, dateFormatAnnotation.get());
        }
        return Optional.of(getDefaultDateTimeFormat(type));
    }

    private static Optional<TransformInfo> getDateFormat(Type type, AnnotationInstance annotationInstance) {
        if (annotationInstance != null) {
            String format = getStringValue(annotationInstance);
            String locale = getStringValue(annotationInstance, LOCALE);
            return Optional.of(new TransformInfo(TransformInfo.Type.DATE, format, locale, isJsonB(annotationInstance)));
        }
        return Optional.of(getDefaultDateTimeFormat(type));
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

    private static Type getCorrectType(Type type) {

        switch (type.kind()) {
            case PARAMETERIZED_TYPE:
                // Collections
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return getCorrectType(typeInCollection);
            case ARRAY:
                // Array
                Type typeInArray = type.asArrayType().component();
                return getCorrectType(typeInArray);
            default:
                return type;
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

    private static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String ISO_OFFSET_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String ISO_ZONED_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ssZ'['VV']'";
    private static final String ISO_DATE = "yyyy-MM-dd";
    private static final String ISO_TIME = "HH:mm:ss";
    private static final String ISO_OFFSET_TIME = "HH:mm:ssZ";

    private static final String LOCALE = "locale";
}

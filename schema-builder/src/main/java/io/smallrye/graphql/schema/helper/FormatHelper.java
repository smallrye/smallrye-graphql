package io.smallrye.graphql.schema.helper;

import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.model.Formatter;

/**
 * Helping with formats of dates and Numbers
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FormatHelper {

    private FormatHelper() {
    }

    public static boolean isNumberLikeTypeOrCollectionThereOf(Type type) {
        return isTypeOrCollectionThereOf(type,
                Classes.BYTE,
                Classes.BYTE_PRIMATIVE,
                Classes.SHORT,
                Classes.SHORT_PRIMATIVE,
                Classes.INTEGER,
                Classes.INTEGER_PRIMATIVE,
                Classes.BIG_INTEGER,
                Classes.DOUBLE,
                Classes.DOUBLE_PRIMATIVE,
                Classes.BIG_DECIMAL,
                Classes.LONG,
                Classes.LONG_PRIMATIVE,
                Classes.FLOAT,
                Classes.FLOAT_PRIMATIVE);
    }

    public static boolean isDateLikeTypeOrCollectionThereOf(Type type) {
        return isTypeOrCollectionThereOf(type,
                Classes.LOCALDATE,
                Classes.LOCALTIME,
                Classes.LOCALDATETIME,
                Classes.ZONEDDATETIME,
                Classes.OFFSETDATETIME,
                Classes.OFFSETTIME,
                Classes.UTIL_DATE,
                Classes.SQL_DATE);
    }

    // '10:15:30+01:00'
    public static String getDefaultDateTimeFormat(Type type) {
        // return the default dates format
        type = getCorrectType(type);
        if (type.name().equals(Classes.LOCALDATE) || type.name().equals(Classes.UTIL_DATE)
                || type.name().equals(Classes.SQL_DATE)) {
            return ISO_DATE;
        } else if (type.name().equals(Classes.LOCALTIME)) {
            return ISO_TIME;
        } else if (type.name().equals(Classes.OFFSETTIME)) {
            return ISO_OFFSET_TIME;
        } else if (type.name().equals(Classes.LOCALDATETIME)) {
            return ISO_DATE_TIME;
        } else if (type.name().equals(Classes.OFFSETDATETIME)) {
            return ISO_OFFSET_DATE_TIME;
        } else if (type.name().equals(Classes.ZONEDDATETIME)) {
            return ISO_ZONED_DATE_TIME;
        }
        throw new InvalidTypeException(type);
    }

    public static Optional<String> getNumberFormatValue(Annotations annotations) {
        Optional<AnnotationInstance> numberFormatAnnotation = getNumberFormatAnnotation(annotations);
        if (numberFormatAnnotation.isPresent()) {
            AnnotationInstance a = numberFormatAnnotation.get();
            return getFormat(a);
        }
        return Optional.empty();
    }

    public static String getDateFormat(Annotations annotations, Type type) {
        Optional<String> format = Optional.empty();

        Optional<AnnotationInstance> dateFormatAnnotation = getDateFormatAnnotation(annotations);
        if (dateFormatAnnotation.isPresent()) {
            format = getFormat(dateFormatAnnotation.get());
        }

        if (format.isPresent()) {
            return format.get();
        } else {
            // return the default dates format
            return getDefaultDateTimeFormat(type);
        }
    }

    public static Formatter getFormatter(Type type, Annotations annotations) {
        if (isDateLikeTypeOrCollectionThereOf(type)) {
            return getDateFormatter(type, annotations);
        } else if (isNumberLikeTypeOrCollectionThereOf(type)) {
            return getNumberFormatter(annotations);
        }
        return null;
    }

    private static Formatter getNumberFormatter(Annotations annotations) {
        Optional<AnnotationInstance> numberFormatAnnotation = getNumberFormatAnnotation(annotations);
        if (numberFormatAnnotation.isPresent()) {
            return getNumberFormatter(numberFormatAnnotation.get());
        }
        return null;
    }

    private static Formatter getNumberFormatter(AnnotationInstance annotationInstance) {
        if (annotationInstance != null) {
            String format = AnnotationsHelper.getStringValue(annotationInstance);
            String locale = AnnotationsHelper.getStringValue(annotationInstance, LOCALE);
            return new Formatter(Formatter.Type.NUMBER, format, locale);
        }
        return null;
    }

    private static Formatter getDateFormatter(Type type, Annotations annotations) {
        Optional<AnnotationInstance> dateFormatAnnotation = getDateFormatAnnotation(annotations);
        if (dateFormatAnnotation.isPresent()) {
            return getDateFormatter(type, dateFormatAnnotation.get());
        }
        return new Formatter(Formatter.Type.DATE, getDefaultDateTimeFormat(type), null);
    }

    private static Formatter getDateFormatter(Type type, AnnotationInstance annotationInstance) {
        if (annotationInstance != null) {
            String format = AnnotationsHelper.getStringValue(annotationInstance);
            String locale = AnnotationsHelper.getStringValue(annotationInstance, LOCALE);

            return new Formatter(Formatter.Type.DATE, format, locale);
        }
        return new Formatter(Formatter.Type.DATE, getDefaultDateTimeFormat(type), null);
    }

    private static Optional<AnnotationInstance> getDateFormatAnnotation(Annotations annotations) {
        return annotations.getOneOfTheseAnnotation(Annotations.DATE_FORMAT, Annotations.JSONB_DATE_FORMAT);
    }

    private static Optional<AnnotationInstance> getNumberFormatAnnotation(Annotations annotations) {
        return annotations.getOneOfTheseAnnotation(Annotations.NUMBER_FORMAT, Annotations.JSONB_NUMBER_FORMAT);
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

    private static boolean isTypeOrCollectionThereOf(Type type, DotName... valid) {
        switch (type.kind()) {
            case PARAMETERIZED_TYPE:
                // Collections
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return isTypeOrCollectionThereOf(typeInCollection, valid);
            case ARRAY:
                // Array
                Type typeInArray = type.asArrayType().component();
                return isTypeOrCollectionThereOf(typeInArray, valid);
            default:
                for (DotName dotName : valid) {
                    if (type.name().equals(dotName)) {
                        return true;
                    }
                }
                return false;
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

    private static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String ISO_OFFSET_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String ISO_ZONED_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ssZ'['VV']'";
    private static final String ISO_DATE = "yyyy-MM-dd";
    private static final String ISO_TIME = "HH:mm:ss";
    private static final String ISO_OFFSET_TIME = "HH:mm:ssZ";

    private static final String LOCALE = "locale";
}

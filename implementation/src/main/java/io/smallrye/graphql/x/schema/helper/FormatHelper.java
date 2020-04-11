/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.graphql.x.schema.helper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.smallrye.graphql.x.Annotations;
import io.smallrye.graphql.x.Classes;

/**
 * Helping with formats of dates and Numbers
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FormatHelper {

    public boolean isNumberLikeTypeOrCollectionThereOf(Type type) {
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

    public NumberFormat getNumberFormat(Annotations annotations) {
        Optional<AnnotationInstance> numberFormatAnnotation = getNumberFormatAnnotation(annotations);
        if (numberFormatAnnotation.isPresent()) {
            return getNumberFormat(numberFormatAnnotation.get());
        }
        return null;
    }

    public boolean isDateLikeTypeOrCollectionThereOf(Type type) {
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

    public DateTimeFormatter getDateFormat(Type type, Annotations annotations) {
        Optional<AnnotationInstance> dateFormatAnnotation = getDateFormatAnnotation(annotations);
        if (dateFormatAnnotation.isPresent()) {
            return getDateFormat(type, dateFormatAnnotation.get());
        }
        return getDefaultDateTimeFormatter(type);
    }

    public DateTimeFormatter getDateFormat(Type type, AnnotationInstance jsonbDateFormatAnnotation) {
        if (jsonbDateFormatAnnotation != null) {
            AnnotationValue locale = jsonbDateFormatAnnotation.value(LOCALE);
            AnnotationValue format = jsonbDateFormatAnnotation.value();

            if (format == null && locale == null) {
                return getDefaultDateTimeFormatter(type);
            } else if (format == null) {
                return getDefaultDateTimeFormatter(type).withLocale(Locale.forLanguageTag(locale.asString()));
            } else if (locale == null) {
                return DateTimeFormatter.ofPattern(format.asString());
            } else {
                return DateTimeFormatter.ofPattern(format.asString()).withLocale(Locale.forLanguageTag(locale.asString()));
            }
        }
        return getDefaultDateTimeFormatter(type);
    }

    public DateTimeFormatter getDefaultDateTimeFormatter(Type type) {
        // return the default dates format
        type = getCorrectType(type);
        if (type.name().equals(Classes.LOCALDATE) || type.name().equals(Classes.UTIL_DATE)
                || type.name().equals(Classes.SQL_DATE)) {
            return DateTimeFormatter.ISO_DATE;
        } else if (type.name().equals(Classes.LOCALTIME)) {
            return DateTimeFormatter.ISO_TIME;
        } else if (type.name().equals(Classes.OFFSETTIME)) {
            return DateTimeFormatter.ISO_OFFSET_TIME;
        } else if (type.name().equals(Classes.LOCALDATETIME)) {
            return DateTimeFormatter.ISO_DATE_TIME;
        } else if (type.name().equals(Classes.ZONEDDATETIME)) {
            return DateTimeFormatter.ISO_ZONED_DATE_TIME;
        } else if (type.name().equals(Classes.OFFSETDATETIME)) {
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        }
        throw new InvalidDateTypeException(type);
    }

    // '10:15:30+01:00'
    public String getDefaultDateTimeFormat(Type type) {
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
        throw new InvalidDateTypeException(type);
    }

    public Optional<String> getNumberFormatValue(Annotations annotations) {
        Optional<AnnotationInstance> numberFormatAnnotation = getNumberFormatAnnotation(annotations);
        if (numberFormatAnnotation.isPresent()) {
            AnnotationInstance a = numberFormatAnnotation.get();
            return getFormat(a);
        }
        return Optional.empty();
    }

    public String getDateFormat(Annotations annotations, Type type) {
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

    private NumberFormat getNumberFormat(AnnotationInstance jsonbNumberFormatAnnotation) {
        if (jsonbNumberFormatAnnotation != null) {
            AnnotationValue locale = jsonbNumberFormatAnnotation.value(LOCALE);
            AnnotationValue format = jsonbNumberFormatAnnotation.value();

            if (format == null && locale == null) {
                return null;
            } else if (format == null) {
                return NumberFormat.getInstance(Locale.forLanguageTag(locale.asString()));
            } else if (locale == null) {
                return new DecimalFormat(format.asString());
            } else {
                return new DecimalFormat(format.asString(),
                        DecimalFormatSymbols.getInstance(Locale.forLanguageTag(locale.asString())));
            }
        }
        return null;
    }

    private Optional<AnnotationInstance> getDateFormatAnnotation(Annotations annotations) {
        return annotations.getOneOfTheseAnnotation(Annotations.DATE_FORMAT, Annotations.JSONB_DATE_FORMAT);
    }

    private Optional<AnnotationInstance> getNumberFormatAnnotation(Annotations annotations) {
        return annotations.getOneOfTheseAnnotation(Annotations.NUMBER_FORMAT, Annotations.JSONB_NUMBER_FORMAT);
    }

    private Optional<String> getFormat(AnnotationInstance annotationInstance) {
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

    private boolean isTypeOrCollectionThereOf(Type type, DotName... valid) {
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

    private Type getCorrectType(Type type) {

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

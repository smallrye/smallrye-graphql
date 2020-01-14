/*
 * Copyright 2019 Red Hat, Inc.
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

package io.smallrye.graphql.schema.helper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.enterprise.context.Dependent;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Classes;

/**
 * Helping with formats of dates and Numbers
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@Dependent
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

    public NumberFormat getNumberFormat(AnnotationInstance jsonbNumberFormatAnnotation) {
        if (jsonbNumberFormatAnnotation != null) {
            AnnotationValue locale = jsonbNumberFormatAnnotation.value("locale");
            AnnotationValue format = jsonbNumberFormatAnnotation.value();

            if (format == null && locale == null) {
                return NumberFormat.getInstance();
            } else if (format == null) {
                return NumberFormat.getInstance(toLocale(locale.asString()));
            } else if (locale == null) {
                return new DecimalFormat(format.asString());
            } else {
                return new DecimalFormat(format.asString(), DecimalFormatSymbols.getInstance(toLocale(locale.asString())));
            }
        }
        return null;
    }

    public boolean isDateLikeTypeOrCollectionThereOf(Type type) {
        return isTypeOrCollectionThereOf(type,
                Classes.LOCALDATE,
                Classes.LOCALTIME,
                Classes.LOCALDATETIME,
                Classes.UTIL_DATE,
                Classes.SQL_DATE);
    }

    public DateTimeFormatter getDateFormat(Type type, AnnotationInstance jsonbDateFormatAnnotation) {
        if (jsonbDateFormatAnnotation != null) {
            AnnotationValue locale = jsonbDateFormatAnnotation.value("locale");
            AnnotationValue format = jsonbDateFormatAnnotation.value();

            if (format == null && locale == null) {
                return getDefaultDateTimeFormatter(type);
            } else if (format == null) {
                return getDefaultDateTimeFormatter(type).withLocale(toLocale(locale.asString()));
            } else if (locale == null) {
                return DateTimeFormatter.ofPattern(format.asString());
            } else {
                return DateTimeFormatter.ofPattern(format.asString()).withLocale(toLocale(locale.asString()));
            }
        }
        return getDefaultDateTimeFormatter(type);
    }

    public DateTimeFormatter getDefaultDateTimeFormatter(Type type) {
        // return the default dates format
        if (type.name().equals(Classes.LOCALDATE) || type.name().equals(Classes.UTIL_DATE)
                || type.name().equals(Classes.SQL_DATE)) {
            return DateTimeFormatter.ISO_DATE;
        } else if (type.name().equals(Classes.LOCALTIME)) {
            return DateTimeFormatter.ISO_TIME;
        } else if (type.name().equals(Classes.LOCALDATETIME)) {
            return DateTimeFormatter.ISO_DATE_TIME;
        }
        throw new RuntimeException("Not a valid Date Type [" + type.name().toString() + "]");
    }

    public String getDefaultDateTimeFormat(Type type) {
        // return the default dates format
        if (type.name().equals(Classes.LOCALDATE) || type.name().equals(Classes.UTIL_DATE)
                || type.name().equals(Classes.SQL_DATE)) {
            return ISO_DATE;
        } else if (type.name().equals(Classes.LOCALTIME)) {
            return ISO_TIME;
        } else if (type.name().equals(Classes.LOCALDATETIME)) {
            return ISO_DATE_TIME;
        }
        throw new RuntimeException("Not a valid Date Type [" + type.name().toString() + "]");
    }

    private boolean isTypeOrCollectionThereOf(Type type, DotName... valid) {
        switch (type.kind()) {
            case PARAMETERIZED_TYPE:
                // Collections
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return isDateLikeTypeOrCollectionThereOf(typeInCollection);
            case ARRAY:
                // Array
                Type typeInArray = type.asArrayType().component();
                return isDateLikeTypeOrCollectionThereOf(typeInArray);
            default:
                for (DotName dotName : valid) {
                    if (type.name().equals(dotName)) {
                        return true;
                    }
                }
                return false;
        }
    }

    private Locale toLocale(final String locale) {
        if (locale == null) {
            return null;
        }
        final int len = locale.length();
        if (len != 2 && len != 5 && len < 7) {
            throw new IllegalArgumentException("Invalid locale format: " + locale);
        }
        final char ch0 = locale.charAt(0);
        final char ch1 = locale.charAt(1);
        if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
            throw new IllegalArgumentException("Invalid locale format: " + locale);
        }
        if (len == 2) {
            return new Locale(locale, "");
        }
        if (locale.charAt(2) != '_') {
            throw new IllegalArgumentException("Invalid locale format: " + locale);
        }
        final char ch3 = locale.charAt(3);
        if (ch3 == '_') {
            return new Locale(locale.substring(0, 2), "", locale.substring(4));
        }
        final char ch4 = locale.charAt(4);
        if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
            throw new IllegalArgumentException("Invalid locale format: " + locale);
        }
        if (len == 5) {
            return new Locale(locale.substring(0, 2), locale.substring(3, 5));
        }
        if (locale.charAt(5) != '_') {
            throw new IllegalArgumentException("Invalid locale format: " + locale);
        }
        return new Locale(locale.substring(0, 2), locale.substring(3, 5), locale.substring(6));
    }

    private static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ISO_DATE = "yyyy-MM-dd";
    private static final String ISO_TIME = "HH:mm:ss";

}

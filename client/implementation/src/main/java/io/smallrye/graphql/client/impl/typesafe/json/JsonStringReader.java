package io.smallrye.graphql.client.impl.typesafe.json;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import javax.json.JsonString;
import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbNumberFormat;

import org.eclipse.microprofile.graphql.DateFormat;
import org.eclipse.microprofile.graphql.NumberFormat;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.impl.typesafe.reflection.ConstructionInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

class JsonStringReader extends Reader<JsonString> {
    JsonStringReader(TypeInfo type, Location location, JsonString value, FieldInfo field) {
        super(type, location, value, field);
    }

    @Override
    Object read() {
        if (char.class.equals(type.getRawType()) || Character.class.equals(type.getRawType())) {
            if (value.getChars().length() != 1)
                throw GraphQLClientValueHelper.fail(location, value);
            return value.getChars().charAt(0);
        }
        if (String.class.equals(type.getRawType()) || Object.class.equals(type.getRawType())) // TODO CharSequence
            return value.getString();
        if (type.isEnum())
            return enumValue();

        if (java.util.Date.class.equals(this.type.getRawType())) {
            return formattedDate(value.getString());
        }
        if (java.util.UUID.class.equals(this.type.getRawType()))
            return java.util.UUID.fromString(value.getString());

        if (Number.class.isAssignableFrom(this.type.getRawType())
                && field != null &&
                ((field.getAnnotation(NumberFormat.class) != null) ||
                        field.getAnnotation(JsonbNumberFormat.class) != null)) {
            return formattedNumber(value.getString());
        }

        ConstructionInfo constructor = type.scalarConstructor()
                .orElseThrow(() -> GraphQLClientValueHelper.fail(location, value));
        try {
            return constructor.execute(value.getString());
        } catch (Exception e) {
            throw new RuntimeException("can't create scalar " + location, e);
        }
    }

    /**
     * Parse the date taking into account potential date-formatting annotations
     */
    private Date formattedDate(String value) {
        if (field != null) {
            String format = null;
            String locale = null;

            JsonbDateFormat jsonbDateFormat = field.getAnnotation(JsonbDateFormat.class);
            if (jsonbDateFormat != null) {
                format = jsonbDateFormat.value();
                locale = jsonbDateFormat.locale();
            }

            DateFormat dateFormat = field.getAnnotation(DateFormat.class);
            if (dateFormat != null) {
                format = dateFormat.value();
                locale = dateFormat.locale();
            }

            java.text.DateFormat df;
            if (format != null) {
                if (!locale.isEmpty()) {
                    df = new SimpleDateFormat(format, Locale.forLanguageTag(locale));
                } else {
                    df = new SimpleDateFormat(format);
                }
            } else {
                df = new SimpleDateFormat();
            }
            try {
                return df.parse(value);
            } catch (ParseException e) {
                throw new RuntimeException("Cannot parse date", e);
            }
        } else {
            return java.util.Date.from(Instant.parse(value));
        }
    }

    private Number formattedNumber(String input) {
        String locale = null;
        String format = null;

        JsonbNumberFormat jsonbNumberFormat = field.getAnnotation(JsonbNumberFormat.class);
        if (jsonbNumberFormat != null) {
            locale = jsonbNumberFormat.locale();
            format = jsonbNumberFormat.value();
        }

        NumberFormat numberFormat = field.getAnnotation(NumberFormat.class);
        if (numberFormat != null) {
            locale = numberFormat.locale();
            format = numberFormat.value();
        }

        java.text.NumberFormat nf;
        if (format != null && !format.isEmpty()) {
            nf = new DecimalFormat(format);
        } else if (locale != null && !locale.isEmpty()) {
            nf = java.text.NumberFormat.getInstance(Locale.forLanguageTag(locale));
        } else {
            nf = new DecimalFormat();
        }
        try {
            return nf.parse(input);
        } catch (ParseException e) {
            throw new InvalidResponseException("Can't parse number", e);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Enum<?> enumValue() {
        return Enum.valueOf((Class) type.getRawType(), value.getString());
    }
}

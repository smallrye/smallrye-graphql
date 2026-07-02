package io.smallrye.graphql.jackson.jsonb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

import jakarta.json.bind.annotation.JsonbNumberFormat;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class JsonbNumberFormatDeserializer extends ValueDeserializer<Object> {

    private String pattern;
    private String locale;
    private Class<?> targetType;

    public JsonbNumberFormatDeserializer() {
        // Default constructor required by Jackson
    }

    public JsonbNumberFormatDeserializer(String pattern, String locale, Class<?> targetType) {
        this.pattern = pattern;
        this.locale = locale;
        this.targetType = targetType;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null) {
            JsonbNumberFormat ann = property.getAnnotation(JsonbNumberFormat.class);
            if (ann != null) {
                Class<?> rawType = property.getType().getRawClass();
                return new JsonbNumberFormatDeserializer(ann.value(), ann.locale(), rawType);
            }
        }
        return this;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) {
        String text = p.getText();
        Locale loc = resolveLocale(ctxt);
        DecimalFormat fmt = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(loc));
        fmt.setParseBigDecimal(true);

        Number parsed;
        try {
            parsed = fmt.parse(text);
        } catch (ParseException e) {
            throw DatabindException.from(p, "Failed to parse number from '" + text + "' with pattern '" + pattern + "'", e);
        }

        if (targetType == int.class || targetType == Integer.class) {
            return parsed.intValue();
        } else if (targetType == long.class || targetType == Long.class) {
            return parsed.longValue();
        } else if (targetType == double.class || targetType == Double.class) {
            return parsed.doubleValue();
        } else if (targetType == float.class || targetType == Float.class) {
            return parsed.floatValue();
        } else if (targetType == BigDecimal.class) {
            return parsed instanceof BigDecimal ? parsed : new BigDecimal(parsed.toString());
        } else if (targetType == BigInteger.class) {
            return parsed instanceof BigDecimal ? ((BigDecimal) parsed).toBigInteger() : BigInteger.valueOf(parsed.longValue());
        }
        return parsed;
    }

    private Locale resolveLocale(DeserializationContext ctxt) {
        if (locale != null && !locale.isEmpty()
                && !JsonbNumberFormat.DEFAULT_LOCALE.equals(locale)) {
            return Locale.forLanguageTag(locale);
        }
        return ctxt.getConfig().getLocale();
    }
}

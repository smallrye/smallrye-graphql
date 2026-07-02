package io.smallrye.graphql.jackson.jsonb;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import jakarta.json.bind.annotation.JsonbDateFormat;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class JsonbDateFormatDeserializer extends ValueDeserializer<Object> {

    private DateTimeFormatter formatter;
    private Class<?> targetType;

    public JsonbDateFormatDeserializer() {
        // Default constructor required by Jackson
    }

    public JsonbDateFormatDeserializer(String pattern, String locale, Class<?> targetType) {
        Locale loc = (locale == null || locale.isEmpty()) ? Locale.getDefault() : Locale.forLanguageTag(locale);
        this.formatter = DateTimeFormatter.ofPattern(pattern, loc);
        this.targetType = targetType;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null) {
            JsonbDateFormat ann = property.getAnnotation(JsonbDateFormat.class);
            if (ann != null) {
                Class<?> rawType = property.getType().getRawClass();
                return new JsonbDateFormatDeserializer(ann.value(), ann.locale(), rawType);
            }
        }
        return this;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctxt) {
        String text = p.getText();
        TemporalAccessor parsed = formatter.parse(text);

        if (targetType == LocalDate.class) {
            return LocalDate.from(parsed);
        } else if (targetType == LocalDateTime.class) {
            return LocalDateTime.from(parsed);
        } else if (targetType == LocalTime.class) {
            return LocalTime.from(parsed);
        } else if (targetType == OffsetDateTime.class) {
            return OffsetDateTime.from(parsed);
        } else if (targetType == ZonedDateTime.class) {
            return ZonedDateTime.from(parsed);
        } else if (targetType == Instant.class) {
            return Instant.from(parsed);
        } else if (targetType == OffsetTime.class) {
            return OffsetTime.from(parsed);
        }
        return parsed;
    }
}

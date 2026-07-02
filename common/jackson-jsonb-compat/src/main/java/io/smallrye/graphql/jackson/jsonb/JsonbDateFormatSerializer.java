package io.smallrye.graphql.jackson.jsonb;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class JsonbDateFormatSerializer extends ValueSerializer<TemporalAccessor> {

    private final DateTimeFormatter formatter;

    public JsonbDateFormatSerializer(String pattern, String locale) {
        Locale loc = (locale == null || locale.isEmpty()) ? Locale.getDefault() : Locale.forLanguageTag(locale);
        this.formatter = DateTimeFormatter.ofPattern(pattern, loc);
    }

    @Override
    public void serialize(TemporalAccessor value, JsonGenerator gen, SerializationContext serializers) {
        gen.writeString(formatter.format(value));
    }
}

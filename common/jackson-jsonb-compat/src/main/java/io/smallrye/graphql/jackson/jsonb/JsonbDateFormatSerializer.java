package io.smallrye.graphql.jackson.jsonb;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsonbDateFormatSerializer extends JsonSerializer<TemporalAccessor> {

    private final DateTimeFormatter formatter;

    public JsonbDateFormatSerializer(String pattern, String locale) {
        Locale loc = (locale == null || locale.isEmpty()) ? Locale.getDefault() : Locale.forLanguageTag(locale);
        this.formatter = DateTimeFormatter.ofPattern(pattern, loc);
    }

    @Override
    public void serialize(TemporalAccessor value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(formatter.format(value));
    }
}

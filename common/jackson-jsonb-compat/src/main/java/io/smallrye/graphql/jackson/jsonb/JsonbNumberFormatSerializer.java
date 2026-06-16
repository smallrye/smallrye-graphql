package io.smallrye.graphql.jackson.jsonb;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsonbNumberFormatSerializer extends JsonSerializer<Number> {

    private final String pattern;
    private final String locale;

    public JsonbNumberFormatSerializer(String pattern, String locale) {
        this.pattern = pattern;
        this.locale = locale;
    }

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Locale loc = resolveLocale(serializers);
        DecimalFormat fmt = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(loc));
        gen.writeString(fmt.format(value));
    }

    private Locale resolveLocale(SerializerProvider serializers) {
        if (locale != null && !locale.isEmpty()
                && !jakarta.json.bind.annotation.JsonbNumberFormat.DEFAULT_LOCALE.equals(locale)) {
            return Locale.forLanguageTag(locale);
        }
        return serializers.getConfig().getLocale();
    }
}

package io.smallrye.graphql.jackson.jsonb;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class JsonbNumberFormatSerializer extends ValueSerializer<Number> {

    private final String pattern;
    private final String locale;

    public JsonbNumberFormatSerializer(String pattern, String locale) {
        this.pattern = pattern;
        this.locale = locale;
    }

    @Override
    public void serialize(Number value, JsonGenerator gen, SerializationContext serializers) {
        Locale loc = resolveLocale(serializers);
        DecimalFormat fmt = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(loc));
        gen.writeString(fmt.format(value));
    }

    private Locale resolveLocale(SerializationContext serializers) {
        if (locale != null && !locale.isEmpty()
                && !jakarta.json.bind.annotation.JsonbNumberFormat.DEFAULT_LOCALE.equals(locale)) {
            return Locale.forLanguageTag(locale);
        }
        return serializers.getConfig().getLocale();
    }
}

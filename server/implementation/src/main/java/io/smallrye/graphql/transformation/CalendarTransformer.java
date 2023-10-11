package io.smallrye.graphql.transformation;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Transformation;

public class CalendarTransformer implements Transformer<Calendar, String> {

    private final String targetClassName;
    private final DateFormat dateFormat;

    public CalendarTransformer(final Field field, final String targetClassName) {
        this.dateFormat = getDateFormat(field.getTransformation());
        this.targetClassName = targetClassName;
    }

    public CalendarTransformer(final Field field) {
        this(field, field.getReference().getClassName());
    }

    @Override
    public Calendar in(String o) throws Exception {
        if (dateFormat == null) {
            throw msg.notValidDateOrTimeType(targetClassName);
        }
        return new Calendar.Builder().setInstant(dateFormat.parse(o)).build();
    }

    @Override
    public String out(Calendar o) {
        return dateFormat.format(o.getTime());
    }

    private static DateFormat getDateFormat(Transformation formatter) {
        if (formatter == null) {
            // Default format if no formatter is provided
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        }
        String format = formatter.getFormat();
        if (format == null)
            return null;
        String localeTag = formatter.getLocale();

        // Create SimpleDateFormat with the specified format and locale
        Locale locale = (localeTag != null) ? Locale.forLanguageTag(localeTag) : Locale.getDefault();
        return new SimpleDateFormat(format, locale);
    }
}

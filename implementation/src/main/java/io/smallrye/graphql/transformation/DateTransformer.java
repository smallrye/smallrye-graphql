package io.smallrye.graphql.transformation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalQuery;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.TransformInfo;

/**
 * Handles date and time-types from {@linkplain java.time}.
 */
public class DateTransformer implements Transformer {

    private static final Map<String, DateTimeFormatter> DEFAULT_FORMATTER = createDefaultFormatter();
    private static final Map<String, TemporalQuery<?>> TEMPORAL_QUERYS = createTemporalQuerys();
    public static final Set<String> SUPPORTED_TYPES = Collections.unmodifiableSet(DEFAULT_FORMATTER.keySet());

    private final DateTimeFormatter dateTimeFormatter;

    private final String targetClassName;

    public DateTransformer(final Field field, final String targetClassName) {
        this.dateTimeFormatter = getDateFormat(field.getTransformInfo().orElse(null));
        this.targetClassName = targetClassName;
    }

    public DateTransformer(Field field) {
        this(field, field.getReference().getClassName());
    }

    @Override
    public Temporal in(final Object o) {
        TemporalQuery<?> temporalAccessor = TEMPORAL_QUERYS.get(targetClassName);
        DateTimeFormatter dateTimeFormatter = getConcreteFormatter(targetClassName);

        if (temporalAccessor == null || dateTimeFormatter == null) {
            throw new RuntimeException(String.format("[%s] as no valid date or time-type", targetClassName));
        }

        return (Temporal) dateTimeFormatter.parse(o.toString(), temporalAccessor);
    }

    private DateTimeFormatter getConcreteFormatter(String className) {
        if (this.dateTimeFormatter != null) {
            return this.dateTimeFormatter;
        }

        return DEFAULT_FORMATTER.get(className);
    }

    @Override
    public Object out(final Object dateType) {
        if (dateType instanceof Temporal) {
            DateTimeFormatter concreteFormatter = getConcreteFormatter(dateType.getClass().getName());

            if (concreteFormatter != null) {
                return concreteFormatter.format((Temporal) dateType);
            }
        }
        return dateType.toString();
    }

    private static Map<String, TemporalQuery<?>> createTemporalQuerys() {
        Map<String, TemporalQuery<?>> defaultFormatter = new HashMap<>();

        defaultFormatter.put(LocalDate.class.getName(), LocalDate::from);
        defaultFormatter.put(LocalTime.class.getName(), LocalTime::from);
        defaultFormatter.put(LocalDateTime.class.getName(), LocalDateTime::from);
        defaultFormatter.put(OffsetTime.class.getName(), OffsetTime::from);
        defaultFormatter.put(OffsetDateTime.class.getName(), OffsetDateTime::from);
        defaultFormatter.put(ZonedDateTime.class.getName(), ZonedDateTime::from);

        return defaultFormatter;
    }

    private static DateTimeFormatter getDateFormat(TransformInfo formatter) {
        if (formatter != null) {
            String format = formatter.getFormat();
            String locale = formatter.getLocale();
            if (format == null) {
                return null;
            } else if (locale == null) {
                return DateTimeFormatter.ofPattern(format);
            } else {
                return DateTimeFormatter.ofPattern(format).withLocale(Locale.forLanguageTag(locale));
            }
        }
        return null;
    }

    private static Map<String, DateTimeFormatter> createDefaultFormatter() {
        Map<String, DateTimeFormatter> defaultFormatter = new HashMap<>();

        defaultFormatter.put(LocalDate.class.getName(), DateTimeFormatter.ISO_LOCAL_DATE);
        defaultFormatter.put(LocalTime.class.getName(), DateTimeFormatter.ISO_LOCAL_TIME);
        defaultFormatter.put(LocalDateTime.class.getName(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        defaultFormatter.put(OffsetTime.class.getName(), DateTimeFormatter.ISO_OFFSET_TIME);
        defaultFormatter.put(OffsetDateTime.class.getName(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        defaultFormatter.put(ZonedDateTime.class.getName(), DateTimeFormatter.ISO_ZONED_DATE_TIME);

        return defaultFormatter;
    }

}

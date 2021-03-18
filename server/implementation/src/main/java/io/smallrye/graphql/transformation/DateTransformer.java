package io.smallrye.graphql.transformation;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
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
import io.smallrye.graphql.schema.model.Transformation;

/**
 * Handles date and time-types from {@linkplain java.time}.
 */
public class DateTransformer implements Transformer<Temporal, String> {

    private static final Map<String, DateTimeFormatter> DEFAULT_FORMATTER = createDefaultFormatter();
    private static final Map<String, TemporalQuery<?>> TEMPORAL_QUERYS = createTemporalQuerys();
    public static final Set<String> SUPPORTED_TYPES = Collections.unmodifiableSet(DEFAULT_FORMATTER.keySet());

    private final DateTimeFormatter dateTimeFormatter;

    private final String targetClassName;

    public DateTransformer(final Field field, final String targetClassName) {
        this.dateTimeFormatter = getDateFormat(field.getTransformation(), targetClassName);
        this.targetClassName = targetClassName;
    }

    public DateTransformer(Field field) {
        this(field, field.getReference().getClassName());
    }

    @Override
    public Temporal in(final String o) {
        TemporalQuery<?> temporalAccessor = TEMPORAL_QUERYS.get(targetClassName);

        if (temporalAccessor == null || dateTimeFormatter == null) {
            throw msg.notValidDateOrTimeType(targetClassName);
        }

        return (Temporal) dateTimeFormatter.parse(o.toString(), temporalAccessor);
    }

    @Override
    public String out(final Temporal dateType) {
        return dateTimeFormatter.format(dateType);
    }

    private static Map<String, TemporalQuery<?>> createTemporalQuerys() {
        Map<String, TemporalQuery<?>> defaultFormatter = new HashMap<>();

        defaultFormatter.put(LocalDate.class.getName(), LocalDate::from);
        defaultFormatter.put(LocalTime.class.getName(), LocalTime::from);
        defaultFormatter.put(LocalDateTime.class.getName(), LocalDateTime::from);
        defaultFormatter.put(OffsetTime.class.getName(), OffsetTime::from);
        defaultFormatter.put(OffsetDateTime.class.getName(), OffsetDateTime::from);
        defaultFormatter.put(ZonedDateTime.class.getName(), ZonedDateTime::from);
        defaultFormatter.put(Instant.class.getName(), Instant::from);

        return defaultFormatter;
    }

    private static DateTimeFormatter getDateFormat(Transformation formatter, String className) {
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
        return DEFAULT_FORMATTER.get(className);
    }

    private static Map<String, DateTimeFormatter> createDefaultFormatter() {
        Map<String, DateTimeFormatter> defaultFormatter = new HashMap<>();

        defaultFormatter.put(LocalDate.class.getName(), DateTimeFormatter.ISO_LOCAL_DATE);
        defaultFormatter.put(LocalTime.class.getName(), DateTimeFormatter.ISO_LOCAL_TIME);
        defaultFormatter.put(LocalDateTime.class.getName(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        defaultFormatter.put(OffsetTime.class.getName(), DateTimeFormatter.ISO_OFFSET_TIME);
        defaultFormatter.put(OffsetDateTime.class.getName(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        defaultFormatter.put(ZonedDateTime.class.getName(), DateTimeFormatter.ISO_ZONED_DATE_TIME);
        defaultFormatter.put(Instant.class.getName(),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault()));

        return defaultFormatter;
    }
}

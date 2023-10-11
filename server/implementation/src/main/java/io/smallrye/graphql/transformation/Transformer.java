package io.smallrye.graphql.transformation;

import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Transformation;

/**
 * Transforms incoming {@link #in(Object)} and outgoing {@link #out(Object)} objects to correct types and formats.
 *
 * @param <IN> type used in user-classes, eg {@code LocalDate}
 * @param <OUT> type used by graphql, eg {@code String}
 */
public interface Transformer<IN, OUT> {

    UuidTransformer UUID_TRANSFORMER = new UuidTransformer();
    UrlTransformer URL_TRANSFORMER = new UrlTransformer();
    UriTransformer URI_TRANSFORMER = new UriTransformer();
    PeriodTransformer PERIOD_TRANSFORMER = new PeriodTransformer();
    DurationTransformer DURATION_TRANSFORMER = new DurationTransformer();
    CharTransformer CHAR_TRANSFORMER = new CharTransformer();

    static Transformer transformer(Field field) {
        if (field.hasTransformation()) {
            Transformation format = field.getTransformation();
            if (format.getType().equals(Transformation.Type.NUMBER)) {
                return new FormattedNumberTransformer(field);
            } else if (format.getType().equals(Transformation.Type.DATE)) {
                return dateTransformer(field);
            }
        } else if (Classes.isDateLikeType(field.getReference().getClassName())) {
            return dateTransformer(field);
        } else if (Classes.isNumberLikeType(field.getReference().getClassName())) {
            return new NumberTransformer(field);
        } else if (Classes.isCharacter(field.getReference().getClassName())) {
            return CHAR_TRANSFORMER;
        } else if (Classes.isUUID(field.getReference().getClassName())) {
            return UUID_TRANSFORMER;
        } else if (Classes.isURL(field.getReference().getClassName())) {
            return URL_TRANSFORMER;
        } else if (Classes.isURI(field.getReference().getClassName())) {
            return URI_TRANSFORMER;
        } else if (Classes.isDuration(field.getReference().getClassName())) {
            return DURATION_TRANSFORMER;
        } else if (Classes.isPeriod(field.getReference().getClassName())) {
            return PERIOD_TRANSFORMER;
        }

        return null;
    }

    static Transformer dateTransformer(Field field) {
        if (LegacyDateTransformer.SUPPORTED_TYPES.contains(field.getReference().getClassName())) {
            return new LegacyDateTransformer(field);
        }
        if (java.util.Calendar.class.getName().equals(field.getReference().getClassName()) ||
                java.util.GregorianCalendar.class.getName().equals(field.getReference().getClassName())) {
            return new CalendarTransformer(field);
        }
        return new DateTransformer(field);
    }

    IN in(OUT o) throws Exception;

    OUT out(IN o);

}

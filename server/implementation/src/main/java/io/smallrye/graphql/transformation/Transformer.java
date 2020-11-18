package io.smallrye.graphql.transformation;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Transformation;

/**
 * Transforms incoming {@link #in(Object)} and outgoing {@link #out(Object)} objects to correct types and formats.
 * TODO: Caching?
 *
 * @param <IN> type used in user-classes, eg {@code LocalDate}
 * @param <OUT> type used by graphql, eg {@code String}
 */
public interface Transformer<IN, OUT> {

    PassThroughTransformer PASS_THROUGH_TRANSFORMER = new PassThroughTransformer();
    UuidTransformer UUID_TRANSFORMER = new UuidTransformer();
    UrlTransformer URL_TRANSFORMER = new UrlTransformer();
    UriTransformer URI_TRANSFORMER = new UriTransformer();
    PeriodTransformer PERIOD_TRANSFORMER = new PeriodTransformer();
    DurationTransformer DURATION_TRANSFORMER = new DurationTransformer();
    CharTransformer CHAR_TRANSFORMER = new CharTransformer();

    static Object out(Field field, Object object) throws AbstractDataFetcherException {
        if (!shouldTransform(field)) {
            return object;
        }

        try {
            Transformer transformer = Transformer.transformer(field);
            return transformer.out(object);
        } catch (Exception e) {
            log.transformError(e);
            throw new TransformException(e, field, object);
        }
    }

    static Object in(Field field, Object object) throws AbstractDataFetcherException {
        if (!shouldTransform(field)) {
            return object;
        }
        try {
            Transformer transformer = Transformer.transformer(field);
            return transformer.in(object);
        } catch (Exception e) {
            throw new TransformException(e, field, object);
        }
    }

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

        return PASS_THROUGH_TRANSFORMER;
    }

    static Transformer dateTransformer(Field field) {
        if (LegacyDateTransformer.SUPPORTED_TYPES.contains(field.getReference().getClassName())) {
            return new LegacyDateTransformer(field);
        }
        return new DateTransformer(field);
    }

    /**
     * Checks, if this field is a scalar and the object has the wrong type.
     * Transformation is only possible for scalars and only needed if types don't match.
     *
     * @param field the field
     * @return if transformation is needed
     */
    public static boolean shouldTransform(Field field) {
        return field.getReference().getType() == ReferenceType.SCALAR
                && !field.getReference().getClassName().equals(field.getReference().getGraphQlClassName());
    }

    IN in(OUT o) throws Exception;

    OUT out(IN o);

}

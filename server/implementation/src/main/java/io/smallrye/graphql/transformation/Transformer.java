package io.smallrye.graphql.transformation;

import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.TransformInfo;

/**
 * Transforms incoming {@link #in(Object)} and outgoing {@link #out(Object)} objects to correct types and formats.
 * TODO: Caching?
 */
public interface Transformer {

    PassThroughTransformer PASS_THROUGH_TRANSFORMER = new PassThroughTransformer();
    UuidTransformer UUID_TRANSFORMER = new UuidTransformer();
    UrlTransformer URL_TRANSFORMER = new UrlTransformer();
    UriTransformer URI_TRANSFORMER = new UriTransformer();
    PeriodTransformer PERIOD_TRANSFORMER = new PeriodTransformer();
    DurationTransformer DURATION_TRANSFORMER = new DurationTransformer();

    static Transformer transformer(Field field) {
        if (field.hasTransformInfo()) {
            TransformInfo format = field.getTransformInfo();
            if (format.getType().equals(TransformInfo.Type.NUMBER)) {
                if (format.getFormat() != null || format.getLocale() != null) {
                    return new FormattedNumberTransformer(field);
                }
                return new NumberTransformer(field);

            } else if (format.getType().equals(TransformInfo.Type.DATE)) {
                return dateTransformer(field);
            }
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
        } else if (Classes.isDateLikeType(field.getReference().getClassName())) {
            return dateTransformer(field);
        } else if (Classes.isNumberLikeType(field.getReference().getClassName())) {
            return new NumberTransformer(field);
        }

        return PASS_THROUGH_TRANSFORMER;
    }

    static Transformer dateTransformer(Field field) {
        if (LegacyDateTransformer.SUPPORTED_TYPES.contains(field.getReference().getClassName())) {
            return new LegacyDateTransformer(field);
        }
        return new DateTransformer(field);
    }

    static boolean shouldTransform(Field field) {
        return field.hasTransformInfo()
                || Classes.isUUID(field.getReference().getClassName())
                || Classes.isURL(field.getReference().getClassName())
                || Classes.isURI(field.getReference().getClassName())
                || Classes.isPeriod(field.getReference().getClassName())
                || Classes.isDuration(field.getReference().getClassName())
                || Classes.isDateLikeType(field.getReference().getClassName())
                || Classes.isNumberLikeType(field.getReference().getClassName());
    }

    Object in(Object o) throws Exception;

    Object out(Object o);

}

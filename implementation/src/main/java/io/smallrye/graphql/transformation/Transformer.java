package io.smallrye.graphql.transformation;

import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.TransformInfo;

/**
 * Transforms incoming {@link #in(Object)} and outgoing {@link #out(Object)} objects to correct types and formats.
 * TODO: Caching?
 */
public interface Transformer {

    static Transformer transformer(Field field) {
        if (field.getTransformInfo().isPresent()) {
            TransformInfo format = field.getTransformInfo().get();
            if (format.getType().equals(TransformInfo.Type.NUMBER)) {
                if (format.getFormat() != null || format.getLocale() != null) {
                    return new FormattedNumberTransformer(field);
                }
                return new NumberTransformer(field);

            } else if (format.getType().equals(TransformInfo.Type.DATE)) {
                if (LegacyDateTransformer.SUPPORTED_TYPES.contains(field.getReference().getClassName())) {
                    return new LegacyDateTransformer(field);
                }
                return new DateTransformer(field);
            }

        }
        return new PassThroughTransformer();
    }

    Object in(Object o) throws Exception;

    Object out(Object o);

}

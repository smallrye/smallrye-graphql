package io.smallrye.graphql.json;

import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.transformation.BoundedNumberType;

/**
 * An input field whose numeric values must be range checked before the input object is bound.
 *
 * @param field the input field
 * @param numberType the bounded number type to check, or {@code null} if the field is a nested input object whose own
 *        numeric fields are checked recursively
 */
public record NumericInputField(Field field, BoundedNumberType numberType) {
}

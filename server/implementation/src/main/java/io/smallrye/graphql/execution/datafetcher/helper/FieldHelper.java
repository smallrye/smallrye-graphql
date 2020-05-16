package io.smallrye.graphql.execution.datafetcher.helper;

import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.transformation.TransformException;
import io.smallrye.graphql.transformation.Transformer;

/**
 * Help with the field response
 * 
 * Here we need to transform (if needed) the response field
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class FieldHelper extends AbstractHelper {

    private final Field field;

    /**
     * We need the modeled field to create the correct value
     * 
     * @param field the field
     * 
     */
    public FieldHelper(Field field) {
        this.field = field;
    }

    public Object transformResponse(Object argumentValue)
            throws TransformException {
        argumentValue = super.recursiveTransform(argumentValue, field);
        return argumentValue;
    }

    /**
     * By now this is a 'leaf' value, i.e not a collection of array, so we just transform if needed.
     * 
     * @param argumentValue the value to transform
     * @param field the field as created while scanning
     * @return transformed value
     */
    @Override
    Object singleTransform(Object argumentValue, Field field) throws TransformException {
        return Transformer.out(field, argumentValue);
    }

    @Override
    protected Object afterRecursiveTransform(Object fieldValue, Field field) {
        return fieldValue;
    }

    @Override
    protected Class<?> getArrayType(final Field field) {
        return classloadingService.loadClass(field.getReference().getGraphQlClassName());
    }
}

package io.smallrye.graphql.execution.datafetcher.helper;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import io.smallrye.graphql.schema.model.Adapter;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;
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
            throws AbstractDataFetcherException {
        if (!shouldTransform(field)) {
            return argumentValue;
        } else {
            argumentValue = super.recursiveTransform(argumentValue, field);
            return argumentValue;
        }
    }

    /**
     * By now this is a 'leaf' value, i.e not a collection of array, so we just transform if needed.
     * 
     * @param argumentValue the value to transform
     * @param field the field as created while scanning
     * @return transformed value
     */
    @Override
    Object singleTransform(Object argumentValue, Field field) throws AbstractDataFetcherException {
        if (!shouldTransform(field)) {
            return argumentValue;
        } else {
            return transformOutput(field, argumentValue);
        }
    }

    /**
     * By now this is a 'leaf' value, i.e not a collection of array, so we just transform if needed.
     * 
     * @param argumentValue the value to map
     * @param field the field as created while scanning
     * @return mapped value
     */
    @Override
    Object singleMapping(Object argumentValue, Field field) throws AbstractDataFetcherException {
        return argumentValue;
    }

    @Override
    protected Object afterRecursiveTransform(Object fieldValue, Field field) {
        return fieldValue;
    }

    @Override
    protected Class<?> getArrayType(final Field field) {
        return classloadingService.loadClass(field.getReference().getGraphQlClassName());
    }

    private Object transformOutput(Field field, Object object) throws AbstractDataFetcherException {
        if (object == null) {
            return null;
        }
        if (!shouldTransform(field)) {
            return object;
        }

        if (field.hasAdapter()) {
            return transformOutputWithAdapter(field, object);
        } else {
            return transformOutputWithTransformer(field, object);
        }
    }

    /**
     * This is for when a user provided a adapter.
     * 
     * @param field the field definition
     * @param object the pre transform value
     * @return the transformed value
     * @throws AbstractDataFetcherException
     */
    private Object transformOutputWithAdapter(Field field, Object object) throws AbstractDataFetcherException {
        Adapter adapter = field.getAdapter();
        ReflectionInvoker reflectionInvoker = getReflectionInvokerForOutput(adapter);
        try {
            Object adaptedObject = reflectionInvoker.invoke(object);
            return adaptedObject;
        } catch (Exception ex) {
            log.transformError(ex);
            throw new TransformException(ex, field, object);
        }
    }

    /**
     * This is the build in transformation (eg. number and date formatting)
     * 
     * @param field the field definition
     * @param object the pre transform value
     * @return the transformed value
     * @throws AbstractDataFetcherException
     */
    private Object transformOutputWithTransformer(Field field, Object object) throws AbstractDataFetcherException {
        try {
            Transformer transformer = super.getTransformer(field);
            if (transformer == null) {
                return object;
            }
            return transformer.out(object);
        } catch (Exception e) {
            log.transformError(e);
            throw new TransformException(e, field, object);
        }
    }

}

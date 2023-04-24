package io.smallrye.graphql.execution.datafetcher.helper;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.util.List;
import java.util.Map;
import java.util.Set;

import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.schema.model.AdaptWith;
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

    public Object transformOrAdaptResponse(Object argumentValue, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {

        return super.transformOrAdapt(argumentValue, field, dfe);
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
     * By now this is a 'leaf' value, i.e not a collection of array, so we just adapt to if needed.
     *
     * @param argumentValue the value to map
     * @param field the field as created while scanning
     * @return mapped value
     */
    @Override
    Object singleAdapting(Object argumentValue, Field field, DataFetchingEnvironment dfe) throws AbstractDataFetcherException {
        if (argumentValue == null) {
            return null;
        }

        if (field.isAdaptingWith()) {
            AdaptWith adaptWith = field.getAdaptWith();
            ReflectionInvoker reflectionInvoker = getReflectionInvokerForOutput(adaptWith);
            try {
                Object adaptedObject = reflectionInvoker.invoke(argumentValue);
                return adaptedObject;
            } catch (Exception ex) {
                log.transformError(ex);
                throw new TransformException(ex, field, argumentValue);
            }
        } else if (field.isAdaptingTo()) {
            return argumentValue.toString();
        } else if (field.hasWrapper() && field.getWrapper().isMap()) {
            Object key = null;
            Map<String, Object> arguments = dfe.getArguments();
            if (arguments != null && arguments.size() > 0 && arguments.containsKey(KEY)) {
                key = arguments.get(KEY);
            }

            Set entrySet = mapAdapter.to((Map) argumentValue, (List) key, field);

            return recursiveAdapting(entrySet, mapAdapter.getAdaptedField(field), dfe);

        }
        return argumentValue;
    }

    @Override
    protected Object afterRecursiveTransform(Object fieldValue, Field field, DataFetchingEnvironment dfe) {
        return fieldValue;
    }

    @Override
    protected Class<?> getArrayType(final Field field) {
        return classloadingService.loadClass(field.getReference().getGraphQLClassName());
    }

    private Object transformOutput(Field field, Object object) throws AbstractDataFetcherException {
        if (object == null) {
            return null;
        }
        if (!shouldTransform(field)) {
            return object;
        }
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

    private static final String KEY = "key";

}

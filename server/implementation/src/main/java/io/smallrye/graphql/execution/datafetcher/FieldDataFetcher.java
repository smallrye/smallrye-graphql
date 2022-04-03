package io.smallrye.graphql.execution.datafetcher;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.lang.reflect.InvocationTargetException;

import graphql.GraphQLException;
import graphql.TrivialDataFetcher;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeContextManager;
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.spi.ClassloadingService;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * Custom property data fetcher to allow arbitrary method names (instead of getters/setters) and to
 * intercept the result for some manipulation.
 * <p>
 *
 * @param <T>
 * @implNote If the (graphql-) field has no methodName, a {@link FieldAccessor} for the (java-) field is is created.
 *           Otherwise, a {@link MethodAccessor} is created for the accessor method.
 *           <p>
 *           The owner is used to create the {@link PropertyAccessor} independently of the source object (which could be a
 *           different
 *           subtype of the owner class for each call).
 */
public class FieldDataFetcher<T> implements DataFetcher<T>, TrivialDataFetcher<T> {

    private final FieldHelper fieldHelper;
    private final Field field;

    /**
     * Owner of the field.
     */
    private final Reference owner;

    /**
     * PropertyAccessor to access the (java-) field or method.
     */
    private PropertyAccessor<Object> propertyAccessor;

    public FieldDataFetcher(final Field field, final Reference owner) {
        this.fieldHelper = new FieldHelper(field);
        this.field = field;
        this.owner = owner;
    }

    @Override
    public T get(DataFetchingEnvironment dfe) throws Exception {

        SmallRyeContextManager.populateFromDataFetchingEnvironment(field, dfe);

        if (this.propertyAccessor == null) {
            // lazy initialize method handle, does not have to be threadsafe
            this.propertyAccessor = buildPropertyAccessor();
        }

        Object source = dfe.getSource();
        Object resultFromMethodCall = propertyAccessor.get(source);
        try {
            // See if we need to transform
            @SuppressWarnings("unchecked")
            T transformResponse = (T) fieldHelper.transformOrAdaptResponse(resultFromMethodCall, dfe);
            return transformResponse;
        } catch (AbstractDataFetcherException ex) {
            log.transformError(ex);
            @SuppressWarnings("unchecked")
            T result = (T) resultFromMethodCall;
            //TODO: if transformResponse was necessary but failed,
            // resultFromMethodCall would most likely have the wrong type,
            // so this would just produce another error. Better just throw GraphQLException?
            return result;
        }
    }

    /**
     * Creates an {@link PropertyAccessor} for the (graphql-) field, using the (java-) field or method.
     * <p>
     * Accessibility (e.g. {@link java.lang.reflect.Field#canAccess(Object)}}) and types (return tapes and parameters) aren't
     * checked, since the schema builder should only allow public fields and methods.
     *
     * @return a PropertyAccessor to access this property
     */
    private PropertyAccessor<Object> buildPropertyAccessor() {
        try {
            final Class<?> aClass = ClassloadingService.get().loadClass(owner.getClassName());
            if (this.field.getMethodName() == null) {
                return new FieldAccessor<>(aClass.getField(this.field.getPropertyName()));
            }
            return new MethodAccessor<>(aClass.getMethod(this.field.getMethodName()));
        } catch (ReflectiveOperationException e) {
            throw new GraphQLException(e);
        }
    }

    interface PropertyAccessor<T> {
        T get(Object source) throws ReflectiveOperationException;
    }

    static class FieldAccessor<T> implements PropertyAccessor<T> {

        private final java.lang.reflect.Field field;

        FieldAccessor(final java.lang.reflect.Field field) {
            this.field = field;
        }

        @Override
        public T get(final Object source) throws IllegalAccessException {
            @SuppressWarnings("unchecked")
            T result = (T) field.get(source);
            return result;
        }
    }

    static class MethodAccessor<T> implements PropertyAccessor<T> {

        private final java.lang.reflect.Method method;

        MethodAccessor(final java.lang.reflect.Method method) {
            this.method = method;
        }

        @Override
        public T get(final Object source) throws InvocationTargetException, IllegalAccessException {
            @SuppressWarnings("unchecked")
            final T result = (T) method.invoke(source);
            return result;
        }
    }
}

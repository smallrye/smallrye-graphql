package io.smallrye.graphql.execution.datafetcher;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import graphql.GraphQLContext;
import graphql.GraphQLException;
import graphql.TrivialDataFetcher;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.context.SmallRyeContext;
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
 * @implNote method handles are used to get values,instead of plain reflection.
 *           This allows identical access to fields and methods.
 *           If the (graphql-) field has no methodName, a method handle for the (java-) field is is created.
 *           Otherwise, a method handle is created for the accessor method.
 *           <p>
 *           The owner is used to create the method handle independently of the source object (which could be a different
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
     * MethodHandle to access the (java-) field or method.
     */
    private MethodHandle methodHandle;

    public FieldDataFetcher(final Field field, final Reference owner) {
        this.fieldHelper = new FieldHelper(field);
        this.field = field;
        this.owner = owner;
    }

    @Override
    public T get(DataFetchingEnvironment dfe) throws Exception {
        GraphQLContext graphQLContext = dfe.getContext();
        graphQLContext.put("context", ((SmallRyeContext) graphQLContext.get("context")).withDataFromFetcher(dfe, field));

        if (this.methodHandle == null) {
            // lazy initialize method handle, does not have to be threadsafe
            this.methodHandle = buildMethodHandle();
        }

        final Object resultFromMethodCall;
        try {
            Object source = dfe.getSource();
            resultFromMethodCall = methodHandle.invoke(source);
        } catch (Throwable throwable) {
            throw new GraphQLException(throwable);
        }
        try {
            // See if we need to transform
            @SuppressWarnings("unchecked")
            T transformResponse = (T) fieldHelper.transformResponse(resultFromMethodCall);
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
     * Creates an method handle for the (graphql-) field, using the (java-) field or method.
     * <p>
     * Accessibility (e.g. {@link java.lang.reflect.Field#canAccess(Object)}}) and types (return tapes and parameters) aren't
     * checked,
     * since the schema builder should only allow public fields and methods.
     *
     * @return a method handle to access this property
     */
    private MethodHandle buildMethodHandle() {
        try {
            final Class<?> aClass = ClassloadingService.get().loadClass(owner.getClassName());
            final MethodHandles.Lookup lookup = MethodHandles.lookup();
            if (this.field.getMethodName() == null) {
                return lookup.unreflectGetter(aClass.getField(this.field.getPropertyName()));
            }
            return lookup.unreflect(aClass.getMethod(this.field.getMethodName()));
        } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException e) {
            throw new GraphQLException(e);
        }
    }

}

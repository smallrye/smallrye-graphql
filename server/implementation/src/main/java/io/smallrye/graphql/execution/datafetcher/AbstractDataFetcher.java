package io.smallrye.graphql.execution.datafetcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.execution.datafetcher.decorator.DataFetcherDecorator;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.execution.error.GraphQLExceptionWhileDataFetching;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.ClassloadingService;
import io.smallrye.graphql.spi.LookupService;

public abstract class AbstractDataFetcher<T> implements DataFetcher<T> {
    protected final LookupService lookupService = LookupService.load(); // Allows multiple lookup mechanisms
    protected final ClassloadingService classloadingService = ClassloadingService.load(); // Allows multiple classloading mechanisms

    protected final Operation operation;
    protected final FieldHelper fieldHelper;
    protected final ArgumentHelper argumentHelper;
    protected final Collection<DataFetcherDecorator> decorators;
    protected List<Class<?>> parameterClasses;

    /**
     * We use this reflection data fetcher on operations (so Queries, Mutations and Source)
     *
     * ParameterClasses: We need an Array of Classes that this operation method needs so we can use reflection to call the
     * method.
     * FieldHelper: We might have to transform the data on the way out if there was a Formatting annotation on the method,
     * or return object fields, we can not use normal JsonB to do this because we do not bind a full object, and we support
     * annotation that is not part on JsonB
     *
     * ArgumentHelper: The same as above, except for every parameter on the way in.
     *
     * @param operation the operation
     * @param decorators collection of decorators to invoke before and after fetching the data
     *
     */
    protected AbstractDataFetcher(Operation operation, Collection<DataFetcherDecorator> decorators) {
        this.operation = operation;
        this.fieldHelper = new FieldHelper(operation);
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
        this.decorators = decorators;
    }

    /**
     * This makes the call on the method. We do the following:
     * 1) Get the correct instance of the class we want to make the call in using CDI. That allow the developer to still use
     * Scopes in the bean.
     * 2) Get the argument values (if any) from graphql-java and make sue they are in the correct type, and if needed,
     * transformed.
     * 3) Make a call on the method with the correct arguments
     * 4) get the result and if needed transform it before we return it.
     *
     * @param dfe the Data Fetching Environment from graphql-java
     * @return the result from the call.
     *
     * @throws Exception
     */
    @Override
    public abstract T get(final DataFetchingEnvironment dfe) throws Exception;

    protected final DataFetcherResult.Builder<Object> appendPartialResult(
            DataFetcherResult.Builder<Object> resultBuilder,
            DataFetchingEnvironment dfe,
            GraphQLException graphQLException) {
        DataFetcherExceptionHandlerParameters handlerParameters = DataFetcherExceptionHandlerParameters
                .newExceptionParameters()
                .dataFetchingEnvironment(dfe)
                .exception(graphQLException)
                .build();

        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ExecutionPath path = handlerParameters.getPath();
        GraphQLExceptionWhileDataFetching error = new GraphQLExceptionWhileDataFetching(path, graphQLException,
                sourceLocation);

        return resultBuilder
                .data(graphQLException.getPartialResults())
                .error(error);
    }

    /**
     * This created an array of classes needed when calling the method using reflection for argument input.
     * This gets created on construction
     *
     * @return the array of classes.
     */
    protected final Class<?>[] getParameterClasses() {
        if (operation.hasArguments()) {
            if (this.parameterClasses == null) {
                List<Class<?>> cl = new LinkedList<>();
                for (Field argument : operation.getArguments()) {
                    // If the argument is an array / collection, load that class
                    if (argument.hasArray()) {
                        Class<?> clazz = classloadingService.loadClass(argument.getArray().getClassName());
                        cl.add(clazz);
                    } else {
                        Class<?> clazz = classloadingService.loadClass(argument.getReference().getClassName());
                        cl.add(clazz);
                    }
                }
                this.parameterClasses = cl;
            }
            return this.parameterClasses.toArray(new Class[] {});
        }
        return null;
    }

    /**
     * Gets the methode needed by this data fetcher.
     *
     * If the method is not found, an {@code DataFetcherException} is thrown.
     *
     *
     * @param operationClass the class to get the method from
     * @return the method
     * @throws DataFetcherException if a matching method is not found
     */
    protected final Method getMethod(Class<?> operationClass) {
        try {
            return operationClass.getMethod(operation.getMethodName(), getParameterClasses());
        } catch (NoSuchMethodException e) {
            throw SmallRyeGraphQLServerMessages.msg.dataFetcherException(operation, e);
        }
    }

    protected final <R> R execute(ExecutionContext executionContext) throws Exception {
        try {
            return (R) executionContext.proceed();
        } catch (InvocationTargetException ex) {
            //Invoked method has thrown something, unwrap
            Throwable throwable = ex.getCause();

            if (throwable instanceof Error) {
                throw (Error) throwable;
            } else if (throwable instanceof GraphQLException) {
                throw (GraphQLException) throwable;
            } else if (throwable instanceof Exception) {
                throw (Exception) throwable;
            } else {
                throw SmallRyeGraphQLServerMessages.msg.dataFetcherException(operation, throwable);
            }
        }
    }

}

package io.smallrye.graphql.execution.datafetcher;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.lang.reflect.Method;
import java.util.Collection;

import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.GraphQLContext;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.execution.datafetcher.decorator.DataFetcherDecorator;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * Fetch data using some bean lookup and Reflection
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReflectionDataFetcher extends AbstractDataFetcher<DataFetcherResult<Object>> {

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
    public ReflectionDataFetcher(Operation operation, Collection<DataFetcherDecorator> decorators) {
        super(operation, decorators);
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
     */
    @Override
    protected DataFetcherResult<Object> fetch(DataFetchingEnvironment dfe) throws Exception {
        final GraphQLContext context = dfe.getContext();

        final DataFetcherResult.Builder<Object> resultBuilder = DataFetcherResult.newResult().localContext(context);

        Class<?> operationClass = classloadingService.loadClass(operation.getClassName());
        Object declaringObject = lookupService.getInstance(operationClass);
        Method m = getMethod(operationClass);

        try {
            Object[] transformedArguments = argumentHelper.getArguments(dfe);

            ExecutionContextImpl executionContext = new ExecutionContextImpl(declaringObject, m, transformedArguments, context,
                    dfe,
                    decorators.iterator());

            Object resultFromMethodCall = execute(executionContext);

            // See if we need to transform on the way out
            resultBuilder.data(fieldHelper.transformResponse(resultFromMethodCall));
        } catch (AbstractDataFetcherException pe) {
            //Arguments or result couldn't be transformed
            pe.appendDataFetcherResult(resultBuilder, dfe);
        } catch (GraphQLException graphQLException) {
            appendPartialResult(resultBuilder, dfe, graphQLException);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            //m.invoke failed
            throw msg.dataFetcherException(operation, ex);
        }

        return resultBuilder.build();
    }

}

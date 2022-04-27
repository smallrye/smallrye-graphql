package io.smallrye.graphql.execution.datafetcher;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.mutiny.Uni;

/**
 * The default, built in data fetcher
 *
 * @param <K>
 * @param <T>
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DefaultDataFetcher<K, T> extends AbstractDataFetcher<K, T> {

    public DefaultDataFetcher(Operation operation, Type type) {
        super(operation, type);
    }

    @Override
    public <T> T invokeAndTransform(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder,
            Object[] transformedArguments) throws Exception {
        Object resultFromMethodCall = operationInvoker.invoke(transformedArguments);
        Object resultFromTransform = fieldHelper.transformOrAdaptResponse(resultFromMethodCall, dfe);
        resultBuilder.data(resultFromTransform);
        return (T) resultBuilder.build();
    }

    @Override
    public <T> T invokeFailure(DataFetcherResult.Builder<Object> resultBuilder) {
        return (T) resultBuilder.build();
    }

    @Override
    public CompletionStage<List<T>> invokeBatch(DataFetchingEnvironment dfe, Object[] arguments) {
        List<T> resultFromUserCode = callUserMethod(arguments);
        if (resultFromUserCode == null) {
            resultFromUserCode = Collections.EMPTY_LIST;
        }
        return Uni.createFrom().item(resultFromUserCode).subscribeAsCompletionStage();
    }

    private List<T> callUserMethod(final Object[] arguments) {
        try {
            return (List<T>) operationInvoker.invoke(arguments);
        } catch (Exception e) {
            if (e instanceof RuntimeException && e.getCause() != null && !(e.getCause() instanceof RuntimeException)) {
                throw msg.dataFetcherException(operation, e.getCause());
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw msg.dataFetcherException(operation, e);
            }
        }
    }
}

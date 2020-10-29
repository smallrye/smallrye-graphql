package io.smallrye.graphql.spi.datafetcher;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.eclipse.microprofile.graphql.GraphQLException;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.BatchLoaderHelper;
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.execution.datafetcher.helper.PartialResultHelper;
import io.smallrye.graphql.execution.datafetcher.helper.ReflectionHelper;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.DataFetcherService;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * Handle Async calls with CompletableFuture
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CompletableFutureDataFetcherService implements DataFetcherService {
    private Operation operation;
    private FieldHelper fieldHelper;
    private ReflectionHelper reflectionHelper;
    private PartialResultHelper partialResultHelper;
    private ArgumentHelper argumentHelper;
    private EventEmitter eventEmitter;
    private BatchLoaderHelper batchLoaderHelper;

    @Override
    public String getName() {
        return "CompletableFuture (Built-in)";
    }

    @Override
    public String[] forClasses() {
        return new String[] { CompletableFuture.class.getName(), CompletionStage.class.getName() };
    }

    @Override
    public DataFetcherService newInstance() {
        return new CompletableFutureDataFetcherService();
    }

    @Override
    public void init(Operation operation, Config config) {
        this.operation = operation;
        this.eventEmitter = EventEmitter.getInstance(config);
        this.fieldHelper = new FieldHelper(operation);
        this.reflectionHelper = new ReflectionHelper(operation, eventEmitter);
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
        this.partialResultHelper = new PartialResultHelper();
        this.batchLoaderHelper = new BatchLoaderHelper();
    }

    @Override
    public <T> T get(DataFetchingEnvironment dfe, DataFetcherResult.Builder<Object> resultBuilder) throws Exception {
        eventEmitter.fireBeforeDataFetch();

        try {
            Object[] transformedArguments = argumentHelper.getArguments(dfe);
            CompletionStage<Object> futureResultFromMethodCall = reflectionHelper.invoke(transformedArguments);

            return (T) futureResultFromMethodCall.handle((result, throwable) -> {
                if (throwable instanceof CompletionException) {
                    //Exception thrown by underlying method may be wrapped in CompletionException
                    throwable = throwable.getCause();
                }

                if (throwable != null) {
                    eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), throwable);
                    if (throwable instanceof GraphQLException) {
                        GraphQLException graphQLException = (GraphQLException) throwable;
                        partialResultHelper.appendPartialResult(resultBuilder, dfe, graphQLException);
                    } else if (throwable instanceof Exception) {
                        throw msg.dataFetcherException(operation, throwable);
                    } else if (throwable instanceof Error) {
                        throw ((Error) throwable);
                    }
                } else {
                    try {
                        resultBuilder.data(fieldHelper.transformResponse(result));
                    } catch (AbstractDataFetcherException te) {
                        te.appendDataFetcherResult(resultBuilder, dfe);
                    }
                }

                return resultBuilder.build();
            });

        } catch (AbstractDataFetcherException abstractDataFetcherException) {
            //Arguments or result couldn't be transformed
            abstractDataFetcherException.appendDataFetcherResult(resultBuilder, dfe);
            eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), abstractDataFetcherException);
        } catch (GraphQLException graphQLException) {
            partialResultHelper.appendPartialResult(resultBuilder, dfe, graphQLException);
            eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), graphQLException);
        } catch (SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            //m.invoke failed
            eventEmitter.fireOnDataFetchError(dfe.getExecutionId().toString(), ex);
            throw ex;
        } finally {
            eventEmitter.fireAfterDataFetch();
        }

        return (T) CompletableFuture.completedFuture(resultBuilder.build());

    }

    @Override
    public <T> CompletionStage<List<T>> batch(BatchLoaderEnvironment ble, List<Object> keys) {
        Object[] arguments = batchLoaderHelper.getArguments(keys, ble);
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return (CompletableFuture<List<T>>) reflectionHelper.invokePrivileged(tccl, arguments);
    }
}

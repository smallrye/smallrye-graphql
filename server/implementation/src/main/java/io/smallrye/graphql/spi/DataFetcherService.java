package io.smallrye.graphql.spi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
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
import io.smallrye.graphql.transformation.AbstractDataFetcherException;

/**
 * Plugable way to do data fething.
 * We will provide some built in providers.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface DataFetcherService {

    ServiceLoader<DataFetcherService> dataFetcherServices = ServiceLoader.load(DataFetcherService.class);

    Map<String, DataFetcherService> dataFetcherServicesMap = load();

    static DataFetcherService getDataFetcherService(Operation operation) {
        if (operation.getGenericsInfo() != null) {
            String genericsClassName = operation.getGenericsInfo().getHolderClassName();
            if (dataFetcherServicesMap.containsKey(genericsClassName)) {
                return dataFetcherServicesMap.get(genericsClassName).newInstance();
            }
        }

        return new DefaultDataFetcherService();
    }

    static Map<String, DataFetcherService> load() {
        Map<String, DataFetcherService> map = new HashMap<>();

        Iterator<DataFetcherService> it = dataFetcherServices.iterator();
        while (it.hasNext()) {
            DataFetcherService dataFetcherService = it.next();
            for (String className : dataFetcherService.forClasses()) {
                map.put(className, dataFetcherService);
            }
        }

        return map;
    }

    DataFetcherService newInstance();

    String getName();

    String[] forClasses();

    void init(final Operation operation, Config config);

    <T> T get(final DataFetchingEnvironment dfe,
            final DataFetcherResult.Builder<Object> resultBuilder) throws Exception;

    <T> CompletionStage<List<T>> batch(final BatchLoaderEnvironment ble, final List<Object> keys);

    /**
     * Default Lookup service that gets used when none is provided with SPI.
     * This does a synchronous call with reflection
     */
    class DefaultDataFetcherService implements DataFetcherService {

        private FieldHelper fieldHelper;
        private ReflectionHelper reflectionHelper;
        private PartialResultHelper partialResultHelper;
        private ArgumentHelper argumentHelper;
        private EventEmitter eventEmitter;
        private BatchLoaderHelper batchLoaderHelper;

        public DataFetcherService newInstance() {
            return new DefaultDataFetcherService();
        }

        @Override
        public String getName() {
            return "Reflection (default)";
        }

        @Override
        public String[] forClasses() {
            return null; // This is the default, so all non-specified classes
        }

        @Override
        public void init(Operation operation, Config config) {
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
                Object resultFromMethodCall = reflectionHelper.invoke(transformedArguments);
                Object resultFromTransform = fieldHelper.transformResponse(resultFromMethodCall);
                resultBuilder.data(resultFromTransform);
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

            return (T) resultBuilder.build();
        }

        @Override
        public <T> CompletionStage<List<T>> batch(BatchLoaderEnvironment ble, List<Object> keys) {
            Object[] arguments = batchLoaderHelper.getArguments(keys, ble);
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            return CompletableFuture.supplyAsync(() -> (List<T>) reflectionHelper.invokePrivileged(tccl, arguments));
        }
    }
}

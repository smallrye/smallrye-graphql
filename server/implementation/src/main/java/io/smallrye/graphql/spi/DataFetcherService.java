package io.smallrye.graphql.spi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.bootstrap.Config;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.datafetcher.CompletableFutureDataFetcherService;
import io.smallrye.graphql.spi.datafetcher.DefaultDataFetcherService;

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
            loadMapForClasses(map, dataFetcherService);
        }

        // Also load built in Onces
        loadMapForClasses(map, new CompletableFutureDataFetcherService());

        return map;
    }

    static void loadMapForClasses(Map<String, DataFetcherService> map, DataFetcherService dataFetcherService) {
        for (String className : dataFetcherService.forClasses()) {
            map.putIfAbsent(className, dataFetcherService);
        }
    }

    DataFetcherService newInstance();

    String getName();

    String[] forClasses();

    void init(final Operation operation, Config config);

    <T> T get(final DataFetchingEnvironment dfe,
            final DataFetcherResult.Builder<Object> resultBuilder) throws Exception;

    <T> CompletionStage<List<T>> batch(final BatchLoaderEnvironment ble, final List<Object> keys);

}

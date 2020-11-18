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
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Wrapper;
import io.smallrye.graphql.spi.datafetcher.CompletableFutureWrapperHandlerService;
import io.smallrye.graphql.spi.datafetcher.DefaultWrapperHandlerService;

/**
 * Plugable way handle wrappers.
 * 
 * 2 things needed here, how to build the schema and provide a custom datafetcher
 * 
 * We will provide some built in handlers
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface WrapperHandlerService {

    DefaultWrapperHandlerService defaultWrapperHandlerService = new DefaultWrapperHandlerService();

    ServiceLoader<WrapperHandlerService> wrapperHandlerServices = ServiceLoader.load(WrapperHandlerService.class);

    Map<String, WrapperHandlerService> wrapperHandlerServicesMap = load();

    static WrapperHandlerService getWrapperHandlerService(Field field) {
        WrapperHandlerService handlerService = defaultWrapperHandlerService;
        if (field.hasWrapper()) {
            String wrapperClassName = field.getWrapper().getWrapperClassName();
            if (wrapperHandlerServicesMap.containsKey(wrapperClassName)) {
                handlerService = wrapperHandlerServicesMap.get(wrapperClassName);
            }
        }

        return handlerService.newInstance();
    }

    static Map<String, WrapperHandlerService> load() {
        Map<String, WrapperHandlerService> map = new HashMap<>();

        Iterator<WrapperHandlerService> it = wrapperHandlerServices.iterator();
        while (it.hasNext()) {
            WrapperHandlerService wrapperHandlerService = it.next();
            loadMapForClasses(map, wrapperHandlerService);
        }

        // Also load built in Onces
        loadMapForClasses(map, new CompletableFutureWrapperHandlerService());

        return map;
    }

    static void loadMapForClasses(Map<String, WrapperHandlerService> map, WrapperHandlerService wrapperHandlerService) {
        for (String className : wrapperHandlerService.forClasses()) {
            map.putIfAbsent(className, wrapperHandlerService);
        }
    }

    WrapperHandlerService newInstance();

    String getName();

    List<String> forClasses();

    void initDataFetcher(final Operation operation, Config config);

    <T> T getData(final DataFetchingEnvironment dfe,
            final DataFetcherResult.Builder<Object> resultBuilder) throws Exception;

    <T> CompletionStage<List<T>> getBatchData(final BatchLoaderEnvironment ble, final List<Object> keys);

    // For creating the schema, need to know how to unwrap 

    Wrapper unwrap(Field field, boolean isBatch);
}

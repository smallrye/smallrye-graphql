package io.smallrye.graphql.execution.context;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import org.dataloader.BatchLoaderContextProvider;
import org.dataloader.DataLoader;

import graphql.schema.DataFetchingEnvironment;

// hacky way to pass the GraphQL Context from data from data fetchers to BatchLoaderEnvironment
public class SmallRyeBatchLoaderContextProvider implements BatchLoaderContextProvider {

    /**
     * Stores the respective SmallRyeBatchLoaderContextProvider for each DataLoader.
     * <p>
     * WeakHashMap is used to enable garbage collection from both once the request is complete and the DataLoader is no longer
     * in use.
     */
    static final Map<DataLoader, SmallRyeBatchLoaderContextProvider> INSTANCES = Collections
            .synchronizedMap(new WeakHashMap<>());

    public void setDataLoader(DataLoader dataLoader) {
        INSTANCES.put(dataLoader, this);
    }

    public static SmallRyeBatchLoaderContextProvider getForDataLoader(DataLoader dataLoader) {
        return INSTANCES.get(dataLoader);
    }

    private AtomicReference<DataFetchingEnvironment> current = new AtomicReference<>();

    public void set(DataFetchingEnvironment dfe) {
        current.set(dfe);
    }

    @Override
    public DataFetchingEnvironment getContext() {
        return current.getAndSet(null);
    }
}

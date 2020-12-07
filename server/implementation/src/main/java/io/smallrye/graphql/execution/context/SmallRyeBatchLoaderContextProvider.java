package io.smallrye.graphql.execution.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.dataloader.BatchLoaderContextProvider;
import org.dataloader.DataLoader;

// hacky way to pass the SmallRyeContext from data from data fetchers to BatchLoaderEnvironment
public class SmallRyeBatchLoaderContextProvider implements BatchLoaderContextProvider {

    static final Map<DataLoader, SmallRyeBatchLoaderContextProvider> INSTANCES = new ConcurrentHashMap<>();

    public void setDataLoader(DataLoader dataLoader) {
        INSTANCES.put(dataLoader, this);
    }

    public static SmallRyeBatchLoaderContextProvider getForDataLoader(DataLoader dataLoader) {
        return INSTANCES.get(dataLoader);
    }

    private AtomicReference<SmallRyeContext> current = new AtomicReference<>();

    public void set(SmallRyeContext context) {
        current.set(context);
    }

    @Override
    public Object getContext() {
        return current.getAndSet(null);
    }
}

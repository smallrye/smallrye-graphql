package io.smallrye.graphql.execution;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.spi.EventingService;

public class TestEventingService implements EventingService {
    public static Context beforeExecuteContext;
    public static Context afterExecuteContext;
    public static final String KEY = "TestEventingService.enabled";;

    public static void reset() {
        beforeExecuteContext = null;
        afterExecuteContext = null;
    }

    @Override
    public String getConfigKey() {
        return KEY;
    }

    @Override
    public void beforeExecute(Context context) {
        beforeExecuteContext = context;
    }

    @Override
    public void afterExecute(Context context) {
        afterExecuteContext = context;
    }

}

package io.smallrye.graphql.execution.event;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.execution.event.Priorities;
import io.smallrye.graphql.spi.EventingService;

import javax.annotation.Priority;

public class TestEventingService implements EventingService {

    public static int invocationOrder = -1;
    public static Context beforeExecuteContext;
    public static Context afterExecuteContext;
    public static final String KEY = "smallrye.graphql.events.enabled";

    public static void reset() {
        beforeExecuteContext = null;
        afterExecuteContext = null;
        invocationOrder = -1;
    }

    @Override
    public String getConfigKey() {
        return KEY;
    }

    @Override
    public void beforeExecute(Context context) {
        invocationOrder = EventEmitterTest.counter++;
        beforeExecuteContext = context;
    }

    @Override
    public void afterExecute(Context context) {
        invocationOrder = EventEmitterTest.counter++;
        afterExecuteContext = context;
    }

}

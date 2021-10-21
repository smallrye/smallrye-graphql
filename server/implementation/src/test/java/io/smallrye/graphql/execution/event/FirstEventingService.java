package io.smallrye.graphql.execution.event;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.spi.EventingService;

import javax.annotation.Priority;

import static io.smallrye.graphql.execution.event.TestEventingService.KEY;

@Priority(Priorities.FIRST_IN_LAST_OUT)
public class FirstEventingService implements EventingService {
    public static int invocationOrder = -1;

    public static void reset() {
        invocationOrder = -1;
    }

    @Override
    public String getConfigKey() {
        return KEY;
    }

    @Override
    public void beforeExecute(Context context) {
        invocationOrder = EventEmitterTest.counter++;
    }

    @Override
    public void afterExecute(Context context) {
        invocationOrder = EventEmitterTest.counter++;
    }
}

package io.smallrye.graphql.cdi.context;

import java.util.Map;

import org.eclipse.microprofile.context.spi.ThreadContextProvider;
import org.eclipse.microprofile.context.spi.ThreadContextSnapshot;

import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.context.SmallRyeContextManager;

public class GraphQLThreadContextProvider implements ThreadContextProvider {
    public static final String TYPE = "MICROPROFILE_GRAPHQL_CONTEXT";

    @Override
    public ThreadContextSnapshot currentContext(Map<String, String> map) {
        SmallRyeContext currentSmallRyeContext = SmallRyeContextManager.getCurrentSmallRyeContext();
        return () -> {
            SmallRyeContext current = restore(currentSmallRyeContext);
            return () -> restore(current);
        };
    }

    @Override
    public ThreadContextSnapshot clearedContext(Map<String, String> map) {
        return () -> {
            SmallRyeContext current = restore(null);
            return () -> restore(current);
        };
    }

    @Override
    public String getThreadContextType() {
        return TYPE;
    }

    private SmallRyeContext restore(SmallRyeContext context) {
        SmallRyeContext currentSmallRyeContext = SmallRyeContextManager.getCurrentSmallRyeContext();
        if (context == null) {
            SmallRyeContextManager.clearCurrentSmallRyeContext();
        } else {
            SmallRyeContextManager.restore(context);
        }
        return currentSmallRyeContext;
    }
}

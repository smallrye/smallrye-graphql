package io.smallrye.graphql.execution.context;

import java.util.Map;

import org.eclipse.microprofile.context.spi.ThreadContextProvider;
import org.eclipse.microprofile.context.spi.ThreadContextSnapshot;

import io.smallrye.graphql.api.Context;

public class SmallRyeContextThreadContextProvider implements ThreadContextProvider {

    public static final String TYPE = "MICROPROFILE_GRAPHQL_CONTEXT";

    @Override
    public ThreadContextSnapshot currentContext(Map<String, String> props) {
        Context captured = SmallRyeContext.getContext();
        return () -> {
            Context current = restore(captured);
            return () -> restore(current);
        };
    }

    private Context restore(Context context) {
        Context currentContext = SmallRyeContext.getContext();
        if (context == null) {
            SmallRyeContext.remove();
        } else {
            SmallRyeContext.setContext((SmallRyeContext) context);
        }
        return currentContext;
    }

    @Override
    public ThreadContextSnapshot clearedContext(Map<String, String> props) {
        return () -> {
            Context current = restore(null);
            return () -> restore(current);
        };
    }

    @Override
    public String getThreadContextType() {
        return TYPE;
    }

}

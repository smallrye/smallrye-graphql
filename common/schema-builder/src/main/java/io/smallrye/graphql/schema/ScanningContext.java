package io.smallrye.graphql.schema;

import org.jboss.jandex.IndexView;

/**
 * A simple registry to hold the current scanning info
 *
 * At this point we only keep the index in the context
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ScanningContext {
    private static final ThreadLocal<ScanningContext> current = new ThreadLocal<>();

    public static void register(IndexView index) {
        ScanningContext registry = new ScanningContext(index);
        current.set(registry);
    }

    public static IndexView getIndex() {
        return current.get().index;
    }

    public static void remove() {
        current.remove();
    }

    private final IndexView index;

    private ScanningContext(final IndexView index) {
        this.index = index;
    }
}
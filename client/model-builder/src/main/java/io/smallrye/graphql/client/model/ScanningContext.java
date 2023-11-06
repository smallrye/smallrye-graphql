package io.smallrye.graphql.client.model;

import org.jboss.jandex.IndexView;

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
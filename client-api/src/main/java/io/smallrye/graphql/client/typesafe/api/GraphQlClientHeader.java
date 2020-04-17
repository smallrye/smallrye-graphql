package io.smallrye.graphql.client.typesafe.api;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.function.Supplier;

public class GraphQlClientHeader {
    private final String name;
    private final Supplier<Object> supplier;

    @SuppressWarnings("unused")
    GraphQlClientHeader() {
        this(null, null);
    }

    public GraphQlClientHeader(String name, Object value) {
        this(name, () -> value);
    }

    public GraphQlClientHeader(String name, Supplier<Object> supplier) {
        this.name = name;
        this.supplier = supplier;
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        assert supplier != null;
        return supplier.get();
    }

    public Map.Entry<String, Object> toEntry() {
        return new SimpleEntry<>(getName(), getValue());
    }
}

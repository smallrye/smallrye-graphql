package io.smallrye.graphql.client.typesafe.api;

import static java.util.Objects.requireNonNull;

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
        this.name = requireNonNull(name);
        this.supplier = requireNonNull(supplier);
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return supplier.get();
    }

    public Map.Entry<String, Object> toEntry() {
        return new SimpleEntry<>(getName(), getValue());
    }
}

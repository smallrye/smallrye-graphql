package io.smallrye.graphql.client.typesafe.api;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Adds a custom header to the http requests sent out by the GraphQL Client.
 * The key is always fix, but the value can also be a {@link Supplier} for dynamic values.
 * The value can be any Object; the GraphQL client calls <code>toString</code> to convert it.
 * When you are in a Java SE environment, you need to add it to the {@link GraphQlClientBuilder};
 * in a CDI application, you can simply write a <code>Producer</code> method or field;
 */
public class GraphQlClientHeader {
    private final String name;
    private final Supplier<Object> supplier;

    /** required for CDI */
    @Deprecated
    @SuppressWarnings("unused")
    GraphQlClientHeader() {
        this.name = null;
        this.supplier = null;
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
        assert supplier != null;
        return supplier.get();
    }

    public Map.Entry<String, Object> toEntry() {
        return new SimpleEntry<>(getName(), getValue());
    }
}

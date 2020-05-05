package io.smallrye.graphql.client.typesafe.impl.json;

import static java.util.Objects.requireNonNull;

import javax.json.JsonValue;

import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

abstract class Reader<T extends JsonValue> {
    protected final TypeInfo type;
    protected final Location location;
    protected final T value;

    Reader(TypeInfo type, Location location, T value) {
        this.type = type;
        this.location = location;
        this.value = requireNonNull(value);
    }

    abstract Object read();
}

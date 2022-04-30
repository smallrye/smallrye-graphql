package io.smallrye.graphql.client.impl.typesafe.json;

import static java.util.Objects.requireNonNull;

import jakarta.json.JsonValue;

import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

abstract class Reader<T extends JsonValue> {
    protected final TypeInfo type;
    protected final Location location;
    protected final T value;
    protected final FieldInfo field;

    Reader(TypeInfo type, Location location, T value, FieldInfo field) {
        this.type = type;
        this.location = location;
        this.value = requireNonNull(value);
        this.field = field;
    }

    abstract Object read();
}

package io.smallrye.graphql.client.typesafe.impl.json;

import static java.util.Objects.requireNonNull;

import javax.json.JsonValue;

import io.smallrye.graphql.client.typesafe.impl.reflection.FieldInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

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

package io.smallrye.graphql.client.impl.json;

import static lombok.AccessLevel.PACKAGE;

import javax.json.JsonValue;

import io.smallrye.graphql.client.impl.reflection.TypeInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = PACKAGE)
abstract class Reader<T extends JsonValue> {
    protected final TypeInfo type;
    protected final Location location;
    protected final @NonNull T value;

    abstract Object read();
}

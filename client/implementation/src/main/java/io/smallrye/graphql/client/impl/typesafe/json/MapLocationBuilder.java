package io.smallrye.graphql.client.impl.typesafe.json;

import java.util.concurrent.atomic.AtomicInteger;

import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

class MapLocationBuilder {
    private final TypeInfo keyType;
    private final TypeInfo valueType;
    private final String baseDescription;
    private final AtomicInteger index = new AtomicInteger();

    MapLocationBuilder(Location location) {
        this.keyType = location.getType().getKeyType();
        this.valueType = location.getType().getValueType();
        this.baseDescription = location.getDescription();
    }

    Location nextKeyLocation() {
        return new Location(keyType, baseDescription + "[" + index.get() + "]");
    }

    Location nextValueLocation() {
        return new Location(valueType, baseDescription + "[" + index.getAndIncrement() + "]");
    }
}

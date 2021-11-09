package io.smallrye.graphql.client.impl.typesafe.json;

import java.util.concurrent.atomic.AtomicInteger;

import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

class IndexedLocationBuilder {
    private final TypeInfo itemType;
    private final String baseDescription;
    private final AtomicInteger index = new AtomicInteger();

    IndexedLocationBuilder(Location location) {
        this.itemType = location.getType().getItemType();
        this.baseDescription = location.getDescription();
    }

    Location nextLocation() {
        return new Location(itemType, baseDescription + "[" + index.getAndIncrement() + "]");
    }
}

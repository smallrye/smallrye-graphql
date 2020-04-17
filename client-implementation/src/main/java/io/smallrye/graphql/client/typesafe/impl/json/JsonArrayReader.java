package io.smallrye.graphql.client.typesafe.impl.json;

import static io.smallrye.graphql.client.typesafe.impl.CollectionUtils.toArray;
import static io.smallrye.graphql.client.typesafe.impl.json.GraphQlClientValueException.check;
import static io.smallrye.graphql.client.typesafe.impl.json.JsonReader.readJson;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collector;

import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;
import lombok.Getter;

class JsonArrayReader extends Reader<JsonArray> {

    @Getter(lazy = true)
    private final Class<?> collectionType = type.getRawType();
    @Getter(lazy = true)
    private final TypeInfo itemType = type.getItemType();

    JsonArrayReader(TypeInfo type, Location location, JsonArray value) {
        super(type, location, value);
    }

    @Override
    Object read() {
        check(location, value, type.isCollection());
        IndexedLocationBuilder locationBuilder = new IndexedLocationBuilder(location);
        return value.stream().map(item -> readItem(locationBuilder, item)).collect(collector());
    }

    private Object readItem(IndexedLocationBuilder locationBuilder, JsonValue itemValue) {
        Location itemLocation = locationBuilder.nextLocation();
        TypeInfo itemType = getItemType();
        if (itemValue.getValueType() == ValueType.NULL && itemType.isNonNull())
            throw new GraphQlClientException("invalid null " + itemLocation);
        return readJson(itemLocation, itemType, itemValue);
    }

    private Collector<Object, ?, ?> collector() {
        if (getCollectionType().isArray()) {
            @SuppressWarnings("unchecked")
            Class<Object> rawItemType = (Class<Object>) getItemType().getRawType();
            return toArray(rawItemType);
        }
        if (Set.class.isAssignableFrom(getCollectionType()))
            return toSet();
        assert List.class.isAssignableFrom(getCollectionType());
        return toList();
    }
}

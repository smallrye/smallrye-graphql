package io.smallrye.graphql.client.typesafe.impl.json;

import static io.smallrye.graphql.client.typesafe.impl.CollectionUtils.toArray;
import static io.smallrye.graphql.client.typesafe.impl.json.GraphQLClientValueException.check;
import static io.smallrye.graphql.client.typesafe.impl.json.JsonReader.readJson;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;

import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientException;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

class JsonArrayReader extends Reader<JsonArray> {

    private Class<?> collectionType;
    private TypeInfo itemType;

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
            throw new GraphQLClientException("invalid null " + itemLocation);
        return readJson(itemLocation, itemType, itemValue);
    }

    private Collector<Object, ?, ?> collector() {
        Class<?> collectionType = getCollectionType();
        if (collectionType.isArray()) {
            @SuppressWarnings("unchecked")
            Class<Object> rawItemType = (Class<Object>) getItemType().getRawType();
            return toArray(rawItemType);
        }
        if (Set.class.isAssignableFrom(collectionType))
            return toSet();
        assert List.class.isAssignableFrom(collectionType) || collectionType.equals(Collection.class)
                : "collection type " + collectionType.getName() + " not supported";
        return toList();
    }

    private Class<?> getCollectionType() {
        if (collectionType == null)
            collectionType = type.getRawType();
        return collectionType;
    }

    private TypeInfo getItemType() {
        if (itemType == null)
            itemType = type.getItemType();
        return itemType;
    }
}

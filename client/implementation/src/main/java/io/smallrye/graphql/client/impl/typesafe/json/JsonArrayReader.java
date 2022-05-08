package io.smallrye.graphql.client.impl.typesafe.json;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.impl.typesafe.CollectionUtils;
import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

class JsonArrayReader extends Reader<JsonArray> {

    private Class<?> collectionType;
    private TypeInfo itemType;

    JsonArrayReader(TypeInfo type, Location location, JsonArray value, FieldInfo field) {
        super(type, location, value, field);
    }

    @Override
    Object read() {
        GraphQLClientValueHelper.check(location, value, type.isCollection());
        IndexedLocationBuilder locationBuilder = new IndexedLocationBuilder(location);
        return value.stream().map(item -> readItem(locationBuilder, item)).collect(collector());
    }

    private Object readItem(IndexedLocationBuilder locationBuilder, JsonValue itemValue) {
        Location itemLocation = locationBuilder.nextLocation();
        TypeInfo it = getItemType();
        if (itemValue.getValueType() == ValueType.NULL && it.isNonNull())
            throw new InvalidResponseException("invalid null " + itemLocation);
        return JsonReader.readJson(itemLocation, it, itemValue, field);
    }

    private Collector<Object, ?, ?> collector() {
        Class<?> ct = getCollectionType();
        if (ct.isArray()) {
            @SuppressWarnings("unchecked")
            Class<Object> rawItemType = (Class<Object>) getItemType().getRawType();
            return CollectionUtils.toArray(rawItemType);
        }
        if (Set.class.isAssignableFrom(ct))
            return toSet();
        assert List.class.isAssignableFrom(ct) || ct.equals(Collection.class)
                : "collection type " + ct.getName() + " not supported";
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

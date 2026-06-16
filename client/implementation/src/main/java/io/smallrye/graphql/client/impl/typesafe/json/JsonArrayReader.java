package io.smallrye.graphql.client.impl.typesafe.json;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.impl.typesafe.CollectionUtils;
import io.smallrye.graphql.client.impl.typesafe.reflection.FieldInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;

class JsonArrayReader extends Reader<ArrayNode> {

    private Class<?> collectionType;
    private TypeInfo itemType;

    JsonArrayReader(TypeInfo type, Location location, ArrayNode value, FieldInfo field) {
        super(type, location, value, field);
    }

    @Override
    Object read() {
        GraphQLClientValueHelper.check(location, value, type.isCollection());
        IndexedLocationBuilder locationBuilder = new IndexedLocationBuilder(location);
        return StreamSupport.stream(value.spliterator(), false)
                .map(item -> readItem(locationBuilder, item))
                .collect(collector());
    }

    private Object readItem(IndexedLocationBuilder locationBuilder, JsonNode itemValue) {
        Location itemLocation = locationBuilder.nextLocation();
        TypeInfo it = getItemType();
        if (itemValue.isNull() && it.isNonNull())
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

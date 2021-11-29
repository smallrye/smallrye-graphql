package io.smallrye.graphql.execution.datafetcher.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.smallrye.graphql.api.Adapter;
import io.smallrye.graphql.api.Entry;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Wrapper;
import io.smallrye.graphql.schema.model.WrapperType;

/**
 * The adapter to change map to Entry Set. Users can also supply their own adapter.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DefaultMapAdapter<K, V> implements Adapter<Map<K, V>, Set<Entry<K, V>>> {

    private final Map<Field, Field> fieldAdaptionMap = new HashMap<>();

    @Override
    public Map<K, V> from(Set<Entry<K, V>> entries) {
        Map<K, V> map = new HashMap<>();
        for (Object e : entries) {
            Map<K, V> graphQLJavaMap = (Map<K, V>) e; // The entry complex type comes from graphql-java as an Map
            map.put((K) graphQLJavaMap.get(KEY), (V) graphQLJavaMap.get(VALUE));
        }
        return map;
    }

    @Override
    public Set<Entry<K, V>> to(Map<K, V> map) {
        Set<Entry<K, V>> entries = new HashSet<>();
        Set<Map.Entry<K, V>> entrySet = map.entrySet();
        for (Map.Entry<K, V> e : entrySet) {
            entries.add(new Entry(e.getKey(), e.getValue()));
            e.getKey();
        }
        return entries;
    }

    public Field getAdaptedField(Field original) {

        if (fieldAdaptionMap.containsKey(original)) {
            return fieldAdaptionMap.get(original);
        }

        Field adaptedField = new Field(original.getMethodName(),
                original.getPropertyName(),
                original.getName(),
                original.getReference());

        Wrapper wrapper = new Wrapper();
        wrapper.setNotEmpty(original.getWrapper().isNotEmpty());
        wrapper.setWrapperType(WrapperType.COLLECTION);
        wrapper.setWrapperClassName(Set.class.getName());
        wrapper.setWrapper(original.getWrapper().getWrapper());
        adaptedField.setWrapper(wrapper);

        adaptedField.setAdaptTo(original.getAdaptTo());
        adaptedField.setDefaultValue(original.getDefaultValue());
        adaptedField.setDescription(original.getDescription());
        adaptedField.setDirectiveInstances(original.getDirectiveInstances());
        adaptedField.setNotNull(original.isNotNull());
        adaptedField.setTransformation(original.getTransformation());

        fieldAdaptionMap.put(original, adaptedField);

        return adaptedField;
    }

    private static final String KEY = "key";
    private static final String VALUE = "value";
}

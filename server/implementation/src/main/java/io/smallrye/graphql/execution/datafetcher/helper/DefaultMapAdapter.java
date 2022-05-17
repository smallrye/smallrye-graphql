package io.smallrye.graphql.execution.datafetcher.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbException;

import io.smallrye.graphql.api.Entry;
import io.smallrye.graphql.json.JsonBCreator;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Wrapper;
import io.smallrye.graphql.schema.model.WrapperType;
import io.smallrye.graphql.spi.ClassloadingService;

/**
 * The adapter to change map to Entry Set.Users can also supply their own adapter.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * @param <K> Key
 * @param <V> Value
 */
public class DefaultMapAdapter<K, V> {

    private final Map<Field, Field> fieldAdaptionMap = new HashMap<>();
    private final ClassloadingService classloadingService = ClassloadingService.get();

    public Map<K, V> from(Set<Entry<K, V>> entries, Field field) {

        Map<K, V> map = new HashMap<>();
        for (Object e : entries) {
            Map<K, V> graphQLJavaMap = (Map<K, V>) e; // The entry complex type comes from graphql-java as an Map

            Map<String, Reference> parametrizedTypeArguments = field.getReference().getClassParametrizedTypes();

            Reference keyReference = parametrizedTypeArguments.get("K");
            Reference valueReference = parametrizedTypeArguments.get("V");

            K k = (K) toObject(keyReference, graphQLJavaMap.get(KEY));
            V v = (V) toObject(valueReference, graphQLJavaMap.get(VALUE));

            map.put(k, v);
        }
        return map;
    }

    public Set<Entry<K, V>> to(Map<K, V> map, List<K> key, Field field) {
        Set<Entry<K, V>> entries = new HashSet<>();
        if (key == null || key.isEmpty()) {
            Set<Map.Entry<K, V>> entrySet = map.entrySet();
            for (Map.Entry<K, V> e : entrySet) {
                entries.add(new Entry(e.getKey(), e.getValue()));
            }
        } else {

            Map<String, Reference> parametrizedTypeArguments = field.getReference().getClassParametrizedTypes();
            Reference keyReference = parametrizedTypeArguments.get("K");

            for (K k : key) {

                k = toObject(keyReference, k);
                V queriedValue = map.get(k);
                if (queriedValue != null) {
                    k = map.keySet().stream().filter(k::equals).findAny().orElse(k);
                    entries.add(new Entry(k, queriedValue));
                }
            }
        }
        return entries;
    }

    private <T> T toObject(Reference reference, T t) {
        ReferenceType type = reference.getType();
        String className = reference.getClassName();
        if (!type.equals(ReferenceType.SCALAR)) {
            String jsonString = JsonBCreator.getJsonB().toJson(t);
            try {
                Jsonb jsonb = JsonBCreator.getJsonB(className);
                Class<?> clazz = classloadingService.loadClass(className);
                return (T) jsonb.fromJson(jsonString, clazz);
            } catch (JsonbException jbe) {
                throw new RuntimeException(jbe);
            }
        }
        return t;
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
        adaptedField.addDirectiveInstances(original.getDirectiveInstances());
        adaptedField.setNotNull(original.isNotNull());
        adaptedField.setTransformation(original.getTransformation());

        fieldAdaptionMap.put(original, adaptedField);

        return adaptedField;
    }

    private static final String KEY = "key";
    private static final String VALUE = "value";
}

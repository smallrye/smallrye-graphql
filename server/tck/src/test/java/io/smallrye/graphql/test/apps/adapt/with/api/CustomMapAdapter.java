package io.smallrye.graphql.test.apps.adapt.with.api;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.smallrye.graphql.api.Adapter;
import io.smallrye.graphql.api.Entry;

/**
 * Using an adapter to String
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CustomMapAdapter implements Adapter<Map<String, String>, Set<Entry<String, String>>> {

    @Override
    public Map<String, String> from(Set entries) {
        System.err.println(">>>>>>> entries: " + entries);
        Map<String, String> map = new HashMap<>();
        for (Object e : entries) {
            Map<String, String> graphQLJavaMap = (Map<String, String>) e; // The entry complex type comes from graphql-java as an Map
            map.put(graphQLJavaMap.get("key"), graphQLJavaMap.get("value"));
        }
        return map;
    }

    @Override
    public Set<Entry<String, String>> to(Map<String, String> map) {
        Set<Entry<String, String>> entries = new HashSet<>();
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        for (Map.Entry<String, String> e : entrySet) {
            entries.add(new Entry(e.getKey(), e.getValue()));
            e.getKey();
        }
        return entries;
    }
}

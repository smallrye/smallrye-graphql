package io.smallrye.graphql.client.typesafe.impl;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collector.Characteristics.IDENTITY_FINISH;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

public final class CollectionUtils {
    private CollectionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <K, V> Collector<Entry<K, V>, MultivaluedMap<K, V>, MultivaluedMap<K, V>> toMultivaluedMap() {
        return new Collector<Entry<K, V>, MultivaluedMap<K, V>, MultivaluedMap<K, V>>() {
            @Override
            public Supplier<MultivaluedMap<K, V>> supplier() {
                return MultivaluedHashMap::new;
            }

            @Override
            public BiConsumer<MultivaluedMap<K, V>, Entry<K, V>> accumulator() {
                return (map, entry) -> map.add(entry.getKey(), entry.getValue());
            }

            @Override
            public BinaryOperator<MultivaluedMap<K, V>> combiner() {
                return (a, b) -> {
                    a.putAll(b);
                    return a;
                };
            }

            @Override
            public Function<MultivaluedMap<K, V>, MultivaluedMap<K, V>> finisher() {
                return Function.identity();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return singleton(IDENTITY_FINISH);
            }
        };
    }

    public static <T> Collector<T, List<T>, T[]> toArray(Class<T> componentType) {
        return new Collector<T, List<T>, T[]>() {
            @Override
            public Supplier<List<T>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<List<T>, T> accumulator() {
                return List::add;
            }

            @Override
            public BinaryOperator<List<T>> combiner() {
                return (a, b) -> {
                    a.addAll(b);
                    return a;
                };
            }

            @Override
            public Function<List<T>, T[]> finisher() {
                return list -> {
                    @SuppressWarnings("unchecked")
                    T[] array = (T[]) Array.newInstance(componentType, 0);
                    return list.toArray(array);
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return emptySet();
            }
        };
    }

    public static boolean nonEmpty(String name) {
        return !name.isEmpty();
    }
}

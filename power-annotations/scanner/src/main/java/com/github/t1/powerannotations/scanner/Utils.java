package com.github.t1.powerannotations.scanner;

import static java.util.Collections.emptySet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jboss.jandex.DotName;

public class Utils {

    static DotName toDotName(Class<?> type) {
        return toDotName(type.getName());
    }

    static DotName toDotName(String typeName) {
        return DotName.createSimple(typeName);
    }

    static <T, K, U> Collector<T, TreeMap<K, U>, TreeMap<K, U>> toTreeMap(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends U> valueMapper) {
        return new Collector<T, TreeMap<K, U>, TreeMap<K, U>>() {
            @Override
            public Supplier<TreeMap<K, U>> supplier() {
                return TreeMap::new;
            }

            @Override
            public BiConsumer<TreeMap<K, U>, T> accumulator() {
                return (map, element) -> map.put(keyMapper.apply(element), valueMapper.apply(element));
            }

            @Override
            public BinaryOperator<TreeMap<K, U>> combiner() {
                return (left, right) -> {
                    left.putAll(right);
                    return left;
                };
            }

            @Override
            public Function<TreeMap<K, U>, TreeMap<K, U>> finisher() {
                return Function.identity();
            }

            @Override
            public Set<Characteristics> characteristics() {
                return emptySet();
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

    /** {@link Stream#ofNullable(Object)} is JDK 9+ */
    static <T> Stream<T> streamOfNullable(T value) {
        return (value == null) ? Stream.empty() : Stream.of(value);
    }

    /** Like JDK 9 <code>Optional::stream</code> */
    public static <T> Stream<T> toStream(Optional<T> optional) {
        return optional.map(Stream::of).orElseGet(Stream::empty);
    }
}

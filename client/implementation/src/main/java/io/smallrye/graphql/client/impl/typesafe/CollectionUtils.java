package io.smallrye.graphql.client.impl.typesafe;

import static java.util.Collections.emptySet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class CollectionUtils {
    private CollectionUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static <T> Collector<T, List<T>, ?> toArray(Class<T> componentType) {
        return new Collector<T, List<T>, Object>() {
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
            public Function<List<T>, Object> finisher() {
                return list -> {
                    int size = list.size();
                    Object array = Array.newInstance(componentType, size);
                    for (int i = 0; i < size; i++) {
                        Array.set(array, i, list.get(i));
                    }
                    return array;
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return emptySet();
            }
        };
    }
}

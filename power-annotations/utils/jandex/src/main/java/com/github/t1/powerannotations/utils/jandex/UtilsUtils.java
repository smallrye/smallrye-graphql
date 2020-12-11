package com.github.t1.powerannotations.utils.jandex;

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

public class UtilsUtils {
    private UtilsUtils() {
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
}

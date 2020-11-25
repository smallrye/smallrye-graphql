package org.jboss.jandex;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

class ReflectionUtils {
    private ReflectionUtils() {
    }

    static <T> T get(Object object, String fieldName) {
        return get(object.getClass(), object, fieldName);
    }

    static <T> T get(Class<?> type, Object object, String fieldName) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            T value = (T) field.get(object);
            return value;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("can't get field '" + fieldName + "'", e);
        }
    }

    static void set(Object object, String fieldName, Object value) {
        set(object.getClass(), object, fieldName, value);
    }

    static void set(Class<?> type, Object object, String fieldName, Object value) {
        try {
            Field field = type.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("can't get field '" + fieldName + "'", e);
        }
    }

    static boolean isUnmodifiable(Map<DotName, List<AnnotationInstance>> map) {
        return UNMODIFIABLE_MAP.equals(map.getClass());
    }

    static <K, V> Map<K, V> modifiable(Map<K, V> map) {
        return get(map, "m");
    }

    static boolean isUnmodifiable(List<AnnotationInstance> list) {
        return UNMODIFIABLE_LIST.equals(list.getClass()) || UNMODIFIABLE_RANDOM_ACCESS_LIST.equals(list.getClass());
    }

    static <T> List<T> modifiable(List<T> list) {
        // UnmodifiableRandomAccessList is a subclass of UnmodifiableList
        list = get(UNMODIFIABLE_LIST, list, "list");
        return list;
    }

    static <T> boolean isArraysArrayList(List<T> list) {
        return ARRAY_LIST.equals(list.getClass());
    }

    private static final Class<?> UNMODIFIABLE_MAP = unmodifiableCollectionClass("Map");
    private static final Class<?> UNMODIFIABLE_LIST = unmodifiableCollectionClass("List");
    private static final Class<?> UNMODIFIABLE_RANDOM_ACCESS_LIST = unmodifiableCollectionClass("RandomAccessList");
    private static final Class<?> ARRAY_LIST = classForName(Arrays.class.getName() + "$ArrayList");

    private static Class<?> unmodifiableCollectionClass(String type) {
        return classForName(Collections.class.getName() + "$Unmodifiable" + type);
    }

    private static Class<?> classForName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}

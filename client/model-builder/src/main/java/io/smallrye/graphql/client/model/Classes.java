package io.smallrye.graphql.client.model;

import static io.smallrye.graphql.client.model.ScanningContext.getIndex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.smallrye.graphql.client.typesafe.api.ErrorOr;
import io.smallrye.graphql.client.typesafe.api.TypesafeResponse;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public class Classes {

    private Classes() {
    }

    public static boolean isParameterized(Type type) {
        return type.kind().equals(Type.Kind.PARAMETERIZED_TYPE);
    }

    public static boolean isTypeVariable(Type type) {
        return type.kind().equals(Type.Kind.TYPE_VARIABLE);
    }

    /**
     * Check if a certain type is Optional
     *
     * @param type the type
     * @return true if it is
     */
    public static boolean isOptional(Type type) {
        return isParameterized(type) && type.name().equals(OPTIONAL) // Normal Optional
                || type.name().equals(LONG_OPTIONAL)
                || type.name().equals(DOUBLE_OPTIONAL)
                || type.name().equals(INTEGER_OPTIONAL);
    }

    public static boolean isEnum(Type type) {
        if (Classes.isClass(type)) {
            ClassInfo clazz = getIndex().getClassByName(type.asClassType().name());
            return clazz != null && clazz.isEnum();
        }
        return false;
    }

    public static boolean isAsync(Type type) {
        return isUni(type)
                || isMulti(type);
    }

    public static boolean isUni(Type type) {
        return type.name().equals(UNI);
    }

    public static boolean isMulti(Type type) {
        return type.name().equals(MULTI);
    }

    public static boolean isPrimitive(Type type) {
        return type.kind().equals(Type.Kind.PRIMITIVE);
    }

    public static boolean isClass(Type type) {
        return type.kind().equals(Type.Kind.CLASS);
    }

    /**
     * Return true if this is an array
     *
     * @param type to check
     * @return if this is an array
     */
    public static boolean isArray(Type type) {
        return type.kind().equals(Type.Kind.ARRAY);
    }

    /**
     * Return true if type is java Collection type which is handled as GraphQL array
     *
     * @param type to check
     * @return if this is a collection
     */
    public static boolean isCollection(Type type) {
        if (isParameterized(type)) {
            ClassInfo clazz = ScanningContext.getIndex().getClassByName(type.name());
            if (clazz == null) {
                // use classloader instead of jandex to handle basic java classes/interfaces
                try {
                    Class<?> clazzLoaded = Classes.class.getClassLoader().loadClass(type.name().toString());
                    return Collection.class.isAssignableFrom(clazzLoaded);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Info not found in Jandex index nor classpath for class name:" + type.name());
                }
            }
            if (KNOWN_COLLECTIONS.contains(clazz.name())) {
                return true;
            }

            // we have to go recursively over all super-interfaces as Jandex provides only direct interfaces
            // implemented in the class itself
            for (Type intf : clazz.interfaceTypes()) {
                if (isCollection(intf)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return true if type is java Map or its implementations (KNOWN_MAPS)
     *
     * @param type to check
     * @return if this is a map
     */
    public static boolean isMap(Type type) {
        if (isParameterized(type)) {

            ClassInfo clazz = ScanningContext.getIndex().getClassByName(type.name());
            if (clazz == null) {
                // use classloader instead of jandex to handle basic java classes/interfaces
                try {
                    Class<?> clazzLoaded = Classes.class.getClassLoader().loadClass(type.name().toString());
                    return Map.class.isAssignableFrom(clazzLoaded);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Info not found in Jandex index nor classpath for class name:" + type.name());
                }
            }
            if (KNOWN_MAPS.contains(clazz.name())) {
                return true;
            }

            // we have to go recursively over all super-interfaces as Jandex provides only direct interfaces
            // implemented in the class itself
            for (Type intf : clazz.interfaceTypes()) {
                if (isMap(intf)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final DotName UNI = DotName.createSimple(Uni.class.getName());
    private static final DotName MULTI = DotName.createSimple(Multi.class.getName());

    public static final DotName OBJECT = DotName.createSimple(Object.class.getName());

    public static final DotName COLLECTION = DotName.createSimple(Collection.class.getName());
    public static final DotName LIST = DotName.createSimple(List.class.getName());
    public static final DotName LINKED_LIST = DotName.createSimple(LinkedList.class.getName());
    public static final DotName VECTOR = DotName.createSimple(Vector.class.getName());
    public static final DotName ARRAY_LIST = DotName.createSimple(ArrayList.class.getName());
    public static final DotName STACK = DotName.createSimple(Stack.class.getName());

    public static final DotName SET = DotName.createSimple(Set.class.getName());
    public static final DotName HASH_SET = DotName.createSimple(HashSet.class.getName());
    public static final DotName SORTED_SET = DotName.createSimple(SortedSet.class.getName());
    public static final DotName TREE_SET = DotName.createSimple(TreeSet.class.getName());

    public static final DotName QUEUE = DotName.createSimple(Queue.class.getName());
    public static final DotName DEQUE = DotName.createSimple(Deque.class.getName());

    public static final DotName MAP = DotName.createSimple(Map.class.getName());
    public static final DotName HASH_MAP = DotName.createSimple(HashMap.class.getName());
    public static final DotName TREE_MAP = DotName.createSimple(TreeMap.class.getName());
    public static final DotName HASHTABLE = DotName.createSimple(Hashtable.class.getName());
    public static final DotName SORTED_MAP = DotName.createSimple(SortedMap.class.getName());

    public static final DotName OPTIONAL = DotName.createSimple(Optional.class.getName());
    public static final DotName TYPESAFE_RESPONSE = DotName.createSimple(TypesafeResponse.class.getName());
    public static final DotName ERROR_OR = DotName.createSimple(ErrorOr.class.getName());

    private static final DotName INTEGER_OPTIONAL = DotName.createSimple(OptionalInt.class.getName());
    private static final DotName DOUBLE_OPTIONAL = DotName.createSimple(OptionalDouble.class.getName());
    private static final DotName LONG_OPTIONAL = DotName.createSimple(OptionalLong.class.getName());

    private static final List<DotName> KNOWN_COLLECTIONS = new ArrayList<>();
    private static final List<DotName> KNOWN_MAPS = new ArrayList<>();
    static {
        KNOWN_COLLECTIONS.add(COLLECTION);
        KNOWN_COLLECTIONS.add(LIST);
        KNOWN_COLLECTIONS.add(LINKED_LIST);
        KNOWN_COLLECTIONS.add(VECTOR);
        KNOWN_COLLECTIONS.add(ARRAY_LIST);
        KNOWN_COLLECTIONS.add(STACK);
        KNOWN_COLLECTIONS.add(SET);
        KNOWN_COLLECTIONS.add(HASH_SET);
        KNOWN_COLLECTIONS.add(SORTED_SET);
        KNOWN_COLLECTIONS.add(TREE_SET);
        KNOWN_COLLECTIONS.add(QUEUE);
        KNOWN_COLLECTIONS.add(DEQUE);

        KNOWN_MAPS.add(MAP);
        KNOWN_MAPS.add(HASH_MAP);
        KNOWN_MAPS.add(TREE_MAP);
        KNOWN_MAPS.add(HASHTABLE);
        KNOWN_MAPS.add(SORTED_MAP);
    }
}

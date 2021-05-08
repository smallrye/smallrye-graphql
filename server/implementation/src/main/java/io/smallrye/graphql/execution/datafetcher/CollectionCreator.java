package io.smallrye.graphql.execution.datafetcher;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.util.*;

import io.smallrye.graphql.spi.ClassloadingService;

/**
 * Helping with creating collection instances
 * This gets the new collection when creating arguments to call the method.
 * 
 * We get a Collection from graph-ql java, and we go through all elements, potentially transforming them,
 * and then we need to create a new collection of the correct type to call the method via reflection.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CollectionCreator {

    private static final ClassloadingService classloadingService = ClassloadingService.get();

    public static Collection<?> newCollection(String className, int initialSize) {
        Class<?> type = classloadingService.loadClass(className);
        return newCollection(type, initialSize);
    }

    /**
     * Creates an empty instance of a non-interface type of collection, or a suitable subclass of
     * the interfaces {@link List}, {@link Collection}, or {@link Set}.
     *
     * @param type the collection class
     * @return the collection
     */
    private static Collection<?> newCollection(Class<?> type, int initialSize) {
        if (type.isAssignableFrom(ArrayList.class)) {
            return new ArrayList<>(initialSize);
        }
        if (type.isAssignableFrom(HashSet.class)) {
            return new HashSet<>((int) (initialSize / 0.75 + 1));
        }
        if (type.isAssignableFrom(ArrayDeque.class)) {
            return new ArrayDeque<>(initialSize);
        }
        if (type.isAssignableFrom(TreeSet.class)) {
            return new TreeSet<>();
        }

        try {
            return (Collection<?>) type.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            log.noArgConstructorMissing(type.getName());
        }

        throw new IllegalArgumentException("Could not create collection if type " + type.getName());
    }
}

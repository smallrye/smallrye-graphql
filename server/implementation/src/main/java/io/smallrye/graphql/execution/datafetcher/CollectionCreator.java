package io.smallrye.graphql.execution.datafetcher;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private static ClassloadingService classloadingService = ClassloadingService.get();

    public static Collection<?> newCollection(String className) {
        Class<?> type = classloadingService.loadClass(className);
        return newCollection(type);
    }

    /**
     * Creates an empty instance of a non-interface type of collection, or a suitable subclass of
     * the interfaces {@link List}, {@link Collection}, or {@link Set}.
     * 
     * @param type the collection class
     * @return the collection
     */
    private static Collection<?> newCollection(Class<?> type) {
        try {
            return (Collection<?>) type.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            log.noArgConstructorMissing(type == null ? "null" : type.getName());
        }
        if (Set.class.isAssignableFrom(type)) {
            return new HashSet<>();
        }
        return new ArrayList<>();
    }
}

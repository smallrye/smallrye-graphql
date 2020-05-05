package io.smallrye.graphql.execution.datafetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;

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
    private static final Logger LOG = Logger.getLogger(CollectionCreator.class.getName());

    private static ClassloadingService classloadingService = ClassloadingService.load();

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
            LOG.debug("Cannot create no-arg instance of [" + (type == null ? "null" : type.getName()) + "]", ex);
        }
        if (Set.class.isAssignableFrom(type)) {
            return new HashSet<>();
        }
        return new ArrayList<>();
    }
}

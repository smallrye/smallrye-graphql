package io.smallrye.graphql.schema.helper;

import io.smallrye.graphql.schema.Annotations;

/**
 * Helping to figure out if we should ignore a field.
 * Looking for the @Ignore and other relevant annotations.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class IgnoreHelper {

    private IgnoreHelper() {
    }

    /**
     * See if we should ignore this.
     * 
     * @param annotations annotations
     * @return true if we should.
     */
    public static boolean shouldIgnore(Annotations annotations) {
        return annotations.containsOneOfTheseAnnotations(Annotations.IGNORE,
                Annotations.JSONB_TRANSIENT);
    }
}
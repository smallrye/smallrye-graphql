package io.smallrye.graphql.schema.helper;

import java.lang.reflect.Modifier;

import org.jboss.jandex.FieldInfo;

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
        return shouldIgnore(annotations, null);
    }

    /**
     * See if we should ignore this.
     * 
     * @param annotations annotations
     * @param fieldInfo field info (if any)
     * @return true if we should.
     */
    public static boolean shouldIgnore(Annotations annotations, FieldInfo fieldInfo) {
        return checkAnnotations(annotations) || checkTransient(fieldInfo);

    }

    private static boolean checkAnnotations(Annotations annotations) {
        return annotations.containsOneOfTheseAnnotations(Annotations.IGNORE,
                Annotations.JAKARTA_JSONB_TRANSIENT,
                Annotations.JAVAX_JSONB_TRANSIENT,
                Annotations.JACKSON_IGNORE);
    }

    private static boolean checkTransient(FieldInfo fieldInfo) {
        if (fieldInfo == null) {
            return false;
        }
        return Modifier.isTransient(fieldInfo.flags());
    }
}

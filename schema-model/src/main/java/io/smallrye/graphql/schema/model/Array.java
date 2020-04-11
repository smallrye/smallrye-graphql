package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Indicate that a Field or Argument is a collection.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Array implements Serializable {
    private final String className; // The Java class name (One of the collection type or Array)
    private final int depth; // The depth of this array
    private boolean notEmpty = false; // Mark this to be not empty

    public Array(String className, int depth) {
        this.className = className;
        this.depth = depth;
    }

    public void markNotEmpty() {
        this.notEmpty = true;
    }

    public String getClassName() {
        return className;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isNotEmpty() {
        return notEmpty;
    }
}

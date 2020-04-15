package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Indicate that a Field or Argument is a collection.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Array implements Serializable {
    private final String className; // The Java class name (One of the collection types or Array)
    private final Type type; // To differenciate between Collection and Array
    private final int depth; // The depth of this array
    private boolean notEmpty = false; // Mark this to be not empty

    public Array(String className, Type type, int depth) {
        this.className = className;
        this.type = type;
        this.depth = depth;
    }

    public void markNotEmpty() {
        this.notEmpty = true;
    }

    public String getClassName() {
        return this.className;
    }

    public int getDepth() {
        return this.depth;
    }

    public boolean isNotEmpty() {
        return this.notEmpty;
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {
        COLLECTION,
        ARRAY
    }
}

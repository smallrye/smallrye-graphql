package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Indicate that a Field or Argument is a collection.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Array implements Serializable {
    private String className; // The Java class name (One of the collection types or Array)
    private Type type; // To differenciate between Collection and Array
    private int depth; // The depth of this array
    private boolean notEmpty = false; // Mark this to be not empty

    public Array() {
    }

    public Array(String className, Type type, int depth) {
        this.className = className;
        this.type = type;
        this.depth = depth;
    }

    public void setNotEmpty(boolean notEmpty) {
        this.notEmpty = notEmpty;
    }

    public boolean isNotEmpty() {
        return this.notEmpty;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return this.depth;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public enum Type {
        COLLECTION,
        ARRAY
    }
}

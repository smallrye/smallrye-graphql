package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Represents a reference to some other type (type/input/enum/interface)
 * This so that, as we are scanning, we can refer to a type that might not exist yet
 * All types extends this.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Reference implements Serializable {
    private final String className;
    private final String name;
    private final ReferenceType type;

    public Reference(String javaName, String name, ReferenceType type) {
        this.className = javaName;
        this.name = name;
        this.type = type;
    }

    /**
     * This represent the Java Class Name
     * 
     * @return String full class name
     */
    public String getClassName() {
        return className;
    }

    /**
     * This represents the GraphQL Name
     * 
     * @return String name
     */
    public String getName() {
        return name;
    }

    /**
     * This represent the GraphQL Type
     * 
     * @return
     */
    public ReferenceType getType() {
        return type;
    }
}

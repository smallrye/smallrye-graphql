package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Item that has a name and description. (Methods / Fields)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class Item implements Serializable {

    private final String name; // GraphQL Name
    private final String description; // GraphQL Description
    private final String javaName; // Java Name

    public Item(String name, String description, String javaName) {
        this.name = name;
        this.description = description;
        this.javaName = javaName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getJavaName() {
        return javaName;
    }
}

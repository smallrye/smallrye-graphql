package io.smallrye.graphql.schema.model;

/**
 * Item that has a name and description. (Methods / Fields)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class Item {

    private String name; // GraphQL Name
    private String description; // GraphQL Description
    private String javaName; // Java Name

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

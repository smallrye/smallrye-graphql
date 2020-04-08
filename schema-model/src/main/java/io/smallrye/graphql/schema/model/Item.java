package io.smallrye.graphql.schema.model;

/**
 * Item that has a name and description. (Methods / Fields)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class Item {

    private String name;
    private String description;

    public Item(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

package io.smallrye.graphql.schema.model;

/**
 * Represents any entry (type/input/interface/enum) in the schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class Entry extends Reference {
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

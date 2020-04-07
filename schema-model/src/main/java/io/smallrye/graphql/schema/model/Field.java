package io.smallrye.graphql.schema.model;

/**
 * Represents a return (output) to a method
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class Field {
    private String name;
    private String description;
    private boolean mandatory = false;
    private boolean collection = false;

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

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean isCollection() {
        return collection;
    }

    public void setCollection(boolean collection) {
        this.collection = collection;
    }
}
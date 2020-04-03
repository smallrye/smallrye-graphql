package io.smallrye.graphql.schema.model;

/**
 * Represents a return (output) to a method
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class Field extends Root {
    private boolean mandatory = false;
    private boolean collection = false;

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

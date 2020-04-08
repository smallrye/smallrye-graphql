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
    private int collectionDepth = 0;
    private boolean mandatoryInCollection = false;
    private Object defaultValue;

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
        return collectionDepth > 0;
    }

    public void setCollectionDepth(int depth) {
        this.collectionDepth = depth;
    }

    public int getCollectionDepth() {
        return this.collectionDepth;
    }

    public boolean isMandatoryInCollection() {
        return mandatoryInCollection;
    }

    public void setMandatoryInCollection(boolean mandatoryInCollection) {
        this.mandatoryInCollection = mandatoryInCollection;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}
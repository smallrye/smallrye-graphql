package io.smallrye.graphql.schema.model;

/**
 * Represents a return (output) or parameter (input) to a method
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Field extends Item {

    private boolean mandatory = false;
    private int collectionDepth = 0;
    private boolean mandatoryInCollection = false;
    private Object defaultValue;
    private Reference typeReference; // Return type or parameter type

    public Field(String name, String description, String javaName, Reference typeReference) {
        super(name, description, javaName);
        this.typeReference = typeReference;
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

    public Reference getTypeReference() {
        return typeReference;
    }
}
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
    private final Reference typeReference; // Return type or parameter type
    private String javaTypeClassName; // The java class name, possibly the same as typeReference className, except in the case of collection/generics
    private Formatter formatter; // If the field should be formatted

    public Field(String name, String description, String javaName, Reference typeReference, String javaTypeClassName) {
        super(name, description, javaName);
        this.typeReference = typeReference;
        this.javaTypeClassName = javaTypeClassName;
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

    public String getJavaTypeClassName() {
        return javaTypeClassName;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter formatter) {
        this.formatter = formatter;
    }

    public boolean hasFormatter() {
        return this.formatter != null;
    }

}
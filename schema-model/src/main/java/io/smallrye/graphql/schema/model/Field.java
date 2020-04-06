package io.smallrye.graphql.schema.model;

/**
 * Represents a return (output) or parameters to a method
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Field extends Root {
    private boolean mandatory = false;
    private boolean collection = false;
    private Object defaultValue;

    public Field(Reference reference) {
        super.setClassName(reference.getClassName());
        super.setName(reference.getName());
        super.setType(reference.getType());
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

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}

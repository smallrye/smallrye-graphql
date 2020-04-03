package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Represents a parameter (input) to a method
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Parameter implements Serializable {
    private Reference parameterType;
    private String name;
    private String description;
    private Object defaultValue;

    private boolean mandatory = false;
    private boolean collection = false;

    public Reference getParameterType() {
        return parameterType;
    }

    public void setParameterType(Reference parameterType) {
        this.parameterType = parameterType;
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

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
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

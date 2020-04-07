package io.smallrye.graphql.schema.model;

/**
 * Represents a parameter (input) to a method
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Parameter extends Field {
    private Reference parameterType;
    private Object defaultValue;

    public Parameter(String name, String description, Reference parameterType) {
        super.setName(name);
        super.setDescription(description);
        this.parameterType = parameterType;
    }

    public Reference getParameterType() {
        return parameterType;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
}

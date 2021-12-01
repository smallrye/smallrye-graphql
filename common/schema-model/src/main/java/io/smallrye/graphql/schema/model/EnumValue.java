package io.smallrye.graphql.schema.model;

/**
 * Represents one of an enum's values. Is part of {@link EnumType}.
 *
 * @author Felix König (de.felix.koenig@gmail.com)
 */
public final class EnumValue {

    private String description;
    private String value;

    public EnumValue() {
    }

    public EnumValue(String description, String value) {
        this.description = description;
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public String getValue() {
        return value;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EnumValue{" + "description=" + description + ", value=" + value + '}';
    }
}

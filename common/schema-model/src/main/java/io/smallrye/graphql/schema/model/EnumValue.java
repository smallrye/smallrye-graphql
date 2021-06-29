package io.smallrye.graphql.schema.model;

/**
 * Represents one of an enum's values. Is part of {@link EnumType}.
 *
 * @author Felix König (de.felix.koenig@gmail.com)
 */
public final class EnumValue {
    private final String description;
    private final String value;

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
}

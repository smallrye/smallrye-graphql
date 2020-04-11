package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.Optional;

/**
 * Represent a property on a complex type (Type/Input/Interface)
 * 
 * For fields that can take arguments, see Operation.
 * 
 * @see <a href="https://spec.graphql.org/draft/#sec-The-__Field-Type">Field</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Field implements Serializable {

    private final String methodName; // This is the java method name (getter/setter/operation)
    private final String name;
    private final String description;
    private final Reference reference; // The type of this field.

    private boolean notNull = false;
    private Optional<Array> array = Optional.empty(); // If this is a collection
    private Optional<Format> format = Optional.empty(); // If the field should be formatted
    private Optional<String> defaultValue = Optional.empty();

    public Field(String methodName, String name, String description, Reference reference) {
        this.methodName = methodName;
        this.name = name;
        this.description = description;
        this.reference = reference;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Reference getReference() {
        return reference;
    }

    public void markNotNull() {
        this.notNull = true;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public Optional<Array> getArray() {
        return array;
    }

    public void setArray(Optional<Array> array) {
        this.array = array;
    }

    public Optional<Format> getFormat() {
        return format;
    }

    public void setFormat(Optional<Format> format) {
        this.format = format;
    }

    public Optional<String> getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Optional<String> defaultValue) {
        this.defaultValue = defaultValue;
    }
}
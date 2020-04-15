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
    private final String propertyName; // This is the java property name (i.e without get/set/is)
    private final String name;
    private final String description;
    private final Reference reference; // The type of this field.

    private boolean notNull = false;
    private Optional<Array> array = Optional.empty(); // If this is a collection
    private Optional<TransformInfo> transformInfo = Optional.empty(); // If the field should be transformed
    private Optional<Object> defaultValue = Optional.empty();

    public Field(String methodName, String propertyName, String name, String description, Reference reference) {
        this.methodName = methodName;
        this.propertyName = propertyName;
        this.name = name;
        this.description = description;
        this.reference = reference;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getPropertyName() {
        return propertyName;
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

    public Optional<TransformInfo> getTransformInfo() {
        return transformInfo;
    }

    public void setTransformInfo(Optional<TransformInfo> transformInfo) {
        this.transformInfo = transformInfo;
    }

    public Optional<Object> getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Optional<Object> defaultValue) {
        this.defaultValue = defaultValue;
    }
}
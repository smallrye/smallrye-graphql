package io.smallrye.graphql.schema.model;

import java.io.Serializable;

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

    private String methodName; // This is the java method name (getter/setter/operation)
    private String propertyName; // This is the java property name (i.e without get/set/is)
    private String name;
    private String description;
    private Reference reference; // The type of this field.

    private boolean notNull = false;
    private Array array = null; // If this is a collection
    private TransformInfo transformInfo = null; // If the field should be transformed
    private MappingInfo mappingInfo = null; // If the field is mapped to another type
    private String defaultValue = null;

    public Field() {
    }

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

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
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

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public Array getArray() {
        return array;
    }

    public void setArray(Array array) {
        this.array = array;
    }

    public boolean hasArray() {
        return this.array != null;
    }

    public TransformInfo getTransformInfo() {
        return transformInfo;
    }

    public void setTransformInfo(TransformInfo transformInfo) {
        this.transformInfo = transformInfo;
    }

    public boolean hasTransformInfo() {
        return this.transformInfo != null;
    }

    public MappingInfo getMappingInfo() {
        return mappingInfo;
    }

    public void setMappingInfo(MappingInfo mappingInfo) {
        this.mappingInfo = mappingInfo;
    }

    public boolean hasMappingInfo() {
        return this.mappingInfo != null;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean hasDefaultValue() {
        return this.defaultValue != null;
    }

}

package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

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

    /**
     * This is the java method name (getter/setter/operation)
     */
    private String methodName;

    /**
     * This is the java property name (i.e without get/set/is)
     */
    private String propertyName;

    /**
     * This is the GraphQL Name in the schema
     */
    private String name;

    /**
     * This is the description in the GraphQL Schema
     */
    private String description;

    /**
     * The type of this field.
     */
    private Reference reference;

    /**
     * If this is wrapped in generics or an array, this contain the info, examples are arrays, collections, async, optional or
     * just plain generic.
     */
    private Wrapper wrapper = null;

    /**
     * If the field should be transformed
     */
    private Transformation transformation = null;

    /**
     * If the field is mapped to another type
     */
    private AdaptTo adaptTo = null;

    /**
     * If the field contains an adapter
     */
    private AdaptWith adaptWith = null;

    private String defaultValue = null;
    private boolean notNull = false;

    private List<DirectiveInstance> directiveInstances;

    public Field() {
    }

    public Field(String methodName, String propertyName, String name, Reference reference) {
        this.methodName = methodName;
        this.propertyName = propertyName;
        this.name = name;
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

    public Wrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public boolean hasWrapper() {
        return this.wrapper != null;
    }

    public Transformation getTransformation() {
        return transformation;
    }

    public void setTransformation(Transformation transformation) {
        this.transformation = transformation;
    }

    public boolean hasTransformation() {
        return this.transformation != null;
    }

    public AdaptTo getAdaptTo() {
        return adaptTo;
    }

    public void setAdaptTo(AdaptTo adaptTo) {
        this.adaptTo = adaptTo;
    }

    public boolean isAdaptingTo() {
        return this.adaptTo != null;
    }

    public AdaptWith getAdaptWith() {
        return adaptWith;
    }

    public void setAdaptWith(AdaptWith adaptWith) {
        this.adaptWith = adaptWith;
    }

    public boolean isAdaptingWith() {
        return this.adaptWith != null;
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

    public boolean hasDirectiveInstances() {
        return directiveInstances != null && !directiveInstances.isEmpty();
    }

    public List<DirectiveInstance> getDirectiveInstances() {
        return directiveInstances;
    }

    public void setDirectiveInstances(List<DirectiveInstance> directiveInstances) {
        this.directiveInstances = directiveInstances;
    }

    @Override
    public String toString() {
        return "Field{" + "methodName=" + methodName + ", propertyName=" + propertyName + ", name=" + name + ", description="
                + description + ", reference=" + reference + ", wrapper=" + wrapper + ", transformation=" + transformation
                + ", adaptTo=" + adaptTo + ", adaptWith=" + adaptWith + ", defaultValue=" + defaultValue + ", notNull="
                + notNull + ", directiveInstances=" + directiveInstances + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.methodName);
        hash = 73 * hash + Objects.hashCode(this.propertyName);
        hash = 73 * hash + Objects.hashCode(this.name);
        hash = 73 * hash + Objects.hashCode(this.description);
        hash = 73 * hash + Objects.hashCode(this.reference);
        hash = 73 * hash + Objects.hashCode(this.wrapper);
        hash = 73 * hash + Objects.hashCode(this.transformation);
        hash = 73 * hash + Objects.hashCode(this.adaptTo);
        hash = 73 * hash + Objects.hashCode(this.adaptWith);
        hash = 73 * hash + Objects.hashCode(this.defaultValue);
        hash = 73 * hash + (this.notNull ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.directiveInstances);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Field other = (Field) obj;
        if (this.notNull != other.notNull) {
            return false;
        }
        if (!Objects.equals(this.methodName, other.methodName)) {
            return false;
        }
        if (!Objects.equals(this.propertyName, other.propertyName)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.description, other.description)) {
            return false;
        }
        if (!Objects.equals(this.defaultValue, other.defaultValue)) {
            return false;
        }
        if (!Objects.equals(this.reference, other.reference)) {
            return false;
        }
        if (!Objects.equals(this.wrapper, other.wrapper)) {
            return false;
        }
        if (!Objects.equals(this.transformation, other.transformation)) {
            return false;
        }
        if (!Objects.equals(this.adaptTo, other.adaptTo)) {
            return false;
        }
        if (!Objects.equals(this.adaptWith, other.adaptWith)) {
            return false;
        }
        if (!Objects.equals(this.directiveInstances, other.directiveInstances)) {
            return false;
        }
        return true;
    }
}

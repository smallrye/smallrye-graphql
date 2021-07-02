package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.List;

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
    private Mapping mapping = null;

    /**
     * If the field contains an adaptor
     */
    private Adapter adaptor = null;

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

    public Mapping getMapping() {
        return mapping;
    }

    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }

    public boolean hasMapping() {
        return this.mapping != null;
    }

    public Adapter getAdaptor() {
        return adaptor;
    }

    public void setAdaptor(Adapter adaptor) {
        this.adaptor = adaptor;
    }

    public boolean hasAdaptor() {
        return this.adaptor != null;
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
}

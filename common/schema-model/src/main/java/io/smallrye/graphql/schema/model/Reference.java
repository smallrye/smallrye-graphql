package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a reference to some other type (type/input/enum/interface) This so that, as we are scanning, we can refer
 * to a type that might not exist yet. All types extends this.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Reference implements Serializable {

    private String className;
    private String name;
    private ReferenceType type;
    private String graphQlClassName;
    private Mapping mapping = null; // If the type is mapped to another type
    private Map<String, Reference> parametrizedTypeArguments;
    private boolean addParametrizedTypeNameExtension;

    public Reference() {
    }

    public Reference(String className, String name, ReferenceType type, String graphQlClassName,
            Map<String, Reference> parametrizedTypeArguments, boolean addParametrizedTypeNameExtension) {
        this(className, name, type, graphQlClassName, parametrizedTypeArguments, addParametrizedTypeNameExtension, null);
    }

    public Reference(String className, String name, ReferenceType type, String graphQlClassName) {
        this(className, name, type, graphQlClassName, null, false, null);
    }

    public Reference(String className, String name, ReferenceType type, String graphQlClassName,
            Map<String, Reference> parametrizedTypeArguments, boolean addParametrizedTypeNameExtension,
            Mapping mapping) {
        this.className = className;
        this.name = name;
        this.type = type;
        this.graphQlClassName = graphQlClassName;
        this.parametrizedTypeArguments = parametrizedTypeArguments;
        this.mapping = mapping;
        this.addParametrizedTypeNameExtension = addParametrizedTypeNameExtension;
    }

    public Reference(String className, String name, ReferenceType type) {
        this(className, name, type, className, null, false, null);
    }

    public Reference(String className, String name, ReferenceType type, Map<String, Reference> parametrizedTypeArguments,
            boolean addParametrizedTypeNameExtension) {
        this(className, name, type, className, parametrizedTypeArguments, addParametrizedTypeNameExtension, null);
    }

    public Reference(final Reference reference) {
        this(reference.className, reference.name, reference.type, reference.graphQlClassName,
                reference.parametrizedTypeArguments, reference.addParametrizedTypeNameExtension, reference.mapping);
    }

    /**
     * This represent the Java Class Name
     *
     * @return String full class name
     */
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * This represents the GraphQL Name
     * 
     * @return String name
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * This represent the GraphQL Type
     *
     * @return
     */
    public ReferenceType getType() {
        return type;
    }

    public void setType(ReferenceType type) {
        this.type = type;
    }

    /**
     * The class into which this reference can be converted, which can be handled by graphql
     *
     * For example, String for Dates or formatted Numbers, BigInteger for long and BigInteger.
     *
     * Used for transforming.
     *
     * @return full class name
     */
    public String getGraphQlClassName() {
        return graphQlClassName;
    }

    public void setGraphQlClassName(String graphQlClassName) {
        this.graphQlClassName = graphQlClassName;
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

    public Map<String, Reference> getParametrizedTypeArguments() {
        return parametrizedTypeArguments;
    }

    public void setParametrizedTypeArguments(Map<String, Reference> parametrizedTypeArguments) {
        this.parametrizedTypeArguments = parametrizedTypeArguments;
    }

    public boolean isAddParametrizedTypeNameExtension() {
        return addParametrizedTypeNameExtension;
    }

    public void setAddParametrizedTypeNameExtension(boolean addParametrizedTypeNameExtension) {
        this.addParametrizedTypeNameExtension = addParametrizedTypeNameExtension;
    }

    @Override
    public String toString() {
        return "Reference [className=" + className + ", name=" + name + ", type=" + type + ", graphQlClassName="
                + graphQlClassName + ", mapping=" + mapping + ", addParametrizedTypeNameExtension="
                + addParametrizedTypeNameExtension
                + ", parametrizedTypeArguments=" + parametrizedTypeArguments + "]";
    }

}

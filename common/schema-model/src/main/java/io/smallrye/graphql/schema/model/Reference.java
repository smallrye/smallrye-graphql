package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents a reference to some other type (type/input/enum/interface) This so that, as we are scanning, we can refer
 * to a type that might not exist yet All types extends this.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Reference implements Serializable {

    private String className;
    private String name;
    private ReferenceType type;
    private String graphQlClassName;
    private MappingInfo mappingInfo = null; // If the type is mapped to another type
    private Map<String, Reference> parametrizedTypeArguments;

    public Reference() {
    }

    public Reference(String className, String name, ReferenceType type, String graphQlClassName,
            Map<String, Reference> parametrizedTypeArguments) {
        this(className, name, type, graphQlClassName, parametrizedTypeArguments, null);
    }

    public Reference(String className, String name, ReferenceType type, String graphQlClassName,
            Map<String, Reference> parametrizedTypeArguments, MappingInfo mappingInfo) {
        this.className = className;
        this.name = name;
        this.type = type;
        this.graphQlClassName = graphQlClassName;
        this.parametrizedTypeArguments = parametrizedTypeArguments;
        this.mappingInfo = mappingInfo;
    }

    public Reference(String className, String name, ReferenceType type) {
        this(className, name, type, className, null, null);
    }

    public Reference(String className, String name, ReferenceType type, Map<String, Reference> parametrizedTypeArguments) {
        this(className, name, type, className, parametrizedTypeArguments, null);
    }

    public Reference(final Reference reference) {
        this(reference.className, reference.name, reference.type, reference.graphQlClassName,
                reference.parametrizedTypeArguments, reference.mappingInfo);
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

    public MappingInfo getMappingInfo() {
        return mappingInfo;
    }

    public void setMappingInfo(MappingInfo mappingInfo) {
        this.mappingInfo = mappingInfo;
    }

    public boolean hasMappingInfo() {
        return this.mappingInfo != null;
    }

    public Map<String, Reference> getParametrizedTypeArguments() {
        return parametrizedTypeArguments;
    }
}

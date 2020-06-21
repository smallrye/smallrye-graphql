package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Represents a reference to some other type (type/input/enum/interface)
 * This so that, as we are scanning, we can refer to a type that might not exist yet
 * All types extends this.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Reference implements Serializable {

    private String className;
    private String name;
    private ReferenceType type;
    private String graphQlClassName;
    private MappingInfo mappingInfo = null; // If the type is mapped to another type

    public Reference() {
    }

    public Reference(String className, String name, ReferenceType type, String graphQlClassName) {
        this(className, name, type, graphQlClassName, null);
    }

    public Reference(String className, String name, ReferenceType type, String graphQlClassName, MappingInfo mappingInfo) {
        this.className = className;
        this.name = name;
        this.type = type;
        this.graphQlClassName = graphQlClassName;
        this.mappingInfo = mappingInfo;
    }

    public Reference(String className, String name, ReferenceType type) {
        this(className, name, type, className, null);
    }

    public Reference(final Reference reference) {
        this(reference.className, reference.name, reference.type, reference.graphQlClassName, reference.mappingInfo);
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
}

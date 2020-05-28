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
    private String graphQlClassName;
    private String className;
    private String name;
    private ReferenceType type;

    public Reference() {
    }

    public Reference(String javaName, String name, ReferenceType type, String graphQlClassName) {
        this.className = javaName;
        this.name = name;
        this.type = type;
        this.graphQlClassName = graphQlClassName;
    }

    public Reference(String javaName, String name, ReferenceType type) {
        this(javaName, name, type, javaName);
    }

    public Reference(final Reference reference) {
        this(reference.className, reference.name, reference.type, reference.graphQlClassName);
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

}

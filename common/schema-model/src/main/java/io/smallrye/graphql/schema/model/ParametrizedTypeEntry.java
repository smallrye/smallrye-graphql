package io.smallrye.graphql.schema.model;

public class ParametrizedTypeEntry {

    private String declaringClassName;
    private String identifier;
    private Reference reference;

    public ParametrizedTypeEntry() {
    }

    public ParametrizedTypeEntry(String declaringClassName, String identifier, Reference reference) {
        this.declaringClassName = declaringClassName;
        this.identifier = identifier;
        this.reference = reference;
    }

    public String getDeclaringClassName() {
        return declaringClassName;
    }

    public void setDeclaringClassName(String declaringClassName) {
        this.declaringClassName = declaringClassName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return "ParametrizedTypeEntry{" +
                "declaringClassName=" + declaringClassName +
                ", identifier=" + identifier +
                ", reference=" + reference +
                '}';
    }
}

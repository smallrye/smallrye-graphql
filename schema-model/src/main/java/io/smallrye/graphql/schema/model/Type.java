package io.smallrye.graphql.schema.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represent a GraphQL Type.
 * 
 * A Type is one of the options for a response, it's a complex type that contains
 * fields that itself is of a certain type.
 * 
 * It's a Java Bean that we only care about the getter methods and properties.
 * 
 * A Type is a java bean with fields, but can optionally also have operations (queries)
 * that is done with the Source annotation.
 * 
 * A Type can also optionally implements interfaces.
 * 
 * @see <a href="https://spec.graphql.org/draft/#sec-Object">Object</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Type extends Reference {

    private final String description;

    private final Set<Field> fields = new LinkedHashSet<>();
    private final Set<Operation> operations = new LinkedHashSet<>();

    private final Set<Reference> interfaces = new LinkedHashSet<>();

    public Type(String className, String name, String description) {
        super(className, name, ReferenceType.TYPE);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Set<Field> getFields() {
        return fields;
    }

    public void addField(Field field) {
        this.fields.add(field);
    }

    public boolean hasFields() {
        return !this.fields.isEmpty();
    }

    public Set<Operation> getOperations() {
        return operations;
    }

    public void addOperation(Operation operation) {
        this.operations.add(operation);
    }

    public boolean hasOperations() {
        return !this.operations.isEmpty();
    }

    public Set<Reference> getInterfaces() {
        return interfaces;
    }

    public void addInterface(Reference interfaceType) {
        this.interfaces.add(interfaceType);
    }

    public boolean hasInterfaces() {
        return !this.interfaces.isEmpty();
    }
}
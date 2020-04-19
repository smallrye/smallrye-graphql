package io.smallrye.graphql.schema.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a Interface in GraphQL.
 * Can be implemented by a Type or another interface.
 * 
 * @see <a href="https://spec.graphql.org/draft/#sec-Interface">Interface</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class InterfaceType extends Reference {

    private String description;
    private Set<Reference> interfaces = new LinkedHashSet<>();

    private Set<Field> fields = new LinkedHashSet<>();

    public InterfaceType() {
    }

    public InterfaceType(String className, String name, String description) {
        super(className, name, ReferenceType.INTERFACE);
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

package io.smallrye.graphql.schema.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
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

    private Map<String, Field> fields = new LinkedHashMap<>();

    public InterfaceType() {
    }

    public InterfaceType(String className, String name, String description) {
        super(className, name, ReferenceType.INTERFACE);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    public void setFields(Map<String, Field> fields) {
        this.fields = fields;
    }

    public void addField(Field field) {
        this.fields.put(field.getName(), field);
    }

    public boolean hasFields() {
        return !this.fields.isEmpty();
    }

    public boolean hasField(String fieldName) {
        return this.fields.containsKey(fieldName);
    }

    public Set<Reference> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Set<Reference> interfaces) {
        this.interfaces = interfaces;
    }

    public void addInterface(Reference interfaceType) {
        this.interfaces.add(interfaceType);
    }

    public boolean hasInterfaces() {
        return !this.interfaces.isEmpty();
    }
}
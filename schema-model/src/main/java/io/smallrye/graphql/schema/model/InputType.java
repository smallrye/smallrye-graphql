package io.smallrye.graphql.schema.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represent a GraphQL Input Type.
 * 
 * A Input Type is used when passing a complex object as an argument in an operation.
 * It's a Java Bean that we only care about the setter methods and properties.
 * 
 * A Input Type is a java bean with fields and setter methods
 * 
 * @see <a href="https://spec.graphql.org/draft/#sec-Object">Object</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class InputType extends Reference {

    private final String description;

    private final Set<Field> fields = new LinkedHashSet<>();

    public InputType(String className, String name, String description) {
        super(className, name, ReferenceType.INPUT);
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
}
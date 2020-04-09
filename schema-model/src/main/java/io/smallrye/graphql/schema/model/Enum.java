package io.smallrye.graphql.schema.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represent an enum type in the Schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Enum extends Root {
    private final Set<String> values = new LinkedHashSet<>();

    public Enum(String className, String name, String description) {
        super(className, name, ReferenceType.ENUM, description);
    }

    public void addValue(String value) {
        this.values.add(value);
    }

    public Set<String> getValues() {
        return this.values;
    }

    public boolean hasValues() {
        return !this.values.isEmpty();
    }
}

package io.smallrye.graphql.schema.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represent an enum in the Schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Enum extends Entry {
    private Set<String> values;

    public Enum(String className) {
        super.setClassName(className);
    }

    public Enum(String className, String name, String description) {
        super.setName(name);
        super.setDescription(description);
        super.setClassName(className);
    }

    public Set<String> getValues() {
        return values;
    }

    public void addValues(String value) {
        if (this.values == null) {
            this.values = new LinkedHashSet<>();
        }
        this.values.add(value);
    }

    public boolean hasValues() {
        return this.values != null && !this.values.isEmpty();
    }
}

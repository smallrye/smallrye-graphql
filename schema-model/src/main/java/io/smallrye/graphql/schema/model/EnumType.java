package io.smallrye.graphql.schema.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represent an enum type in the Schema.
 * 
 * @see <a href="https://spec.graphql.org/draft/#sec-Enum">Enum</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class EnumType extends Reference {
    private String description;
    private Set<String> values = new LinkedHashSet<>();

    public EnumType() {
    }

    public EnumType(String className, String name, String description) {
        super(className, name, ReferenceType.ENUM);
        this.description = description;
    }

    public String getDescription() {
        return description;
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
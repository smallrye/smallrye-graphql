package io.smallrye.graphql.schema.model;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represent a root element in the Schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Definition extends Entry {
    private Set<Field> fields;
    private Set<Reference> interfaces;

    public Definition(String className) {
        super.setClassName(className);
    }

    public Definition(String className, String name, String description) {
        super.setName(name);
        super.setDescription(description);
        super.setClassName(className);
    }

    public Set<Field> getFields() {
        return fields;
    }

    public void addField(Field field) {
        if (this.fields == null) {
            this.fields = new TreeSet<>(comparator);
        }
        this.fields.add(field);
    }

    public boolean hasFields() {
        return this.fields != null && !this.fields.isEmpty();
    }

    public Set<Reference> getInterfaces() {
        return interfaces;
    }

    public void addInterface(Reference interfaceRef) {
        if (this.interfaces == null) {
            this.interfaces = new TreeSet<>(comparator);
        }
        this.interfaces.add(interfaceRef);
    }

    public boolean hasInterfaces() {
        return this.interfaces != null && !this.interfaces.isEmpty();
    }

    private Comparator comparator = new Comparator<Reference>() {
        @Override
        public int compare(Reference o1, Reference o2) {
            if (o1 != null && o2 != null) {
                return o1.getName().compareTo(o2.getName());
            }
            return ZERO;
        }
    };

    private static final int ZERO = 0;
}

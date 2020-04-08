package io.smallrye.graphql.schema.model;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represent a complex root element (type/input/interface) in the Schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Complex extends Root {
    private Set<Method> methods;
    private Set<Method> sources;
    private Set<Reference> interfaces;

    public Complex(String className) {
        super.setClassName(className);
    }

    public Complex(String className, String name, String description) {
        super.setName(name);
        super.setDescription(description);
        super.setClassName(className);
    }

    public Set<Method> getMethods() {
        return methods;
    }

    public void addMethod(Method method) {
        if (this.methods == null) {
            this.methods = new TreeSet<>(comparator);
        }
        this.methods.add(method);
    }

    public boolean hasMethods() {
        return this.methods != null && !this.methods.isEmpty();
    }

    public Set<Method> getSources() {
        return sources;
    }

    public void addSource(Method source) {
        if (this.sources == null) {
            this.sources = new TreeSet<>(comparator);
        }
        this.sources.add(source);
    }

    public boolean hasSources() {
        return this.sources != null && !this.sources.isEmpty();
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

    private final Comparator comparator = new Comparator<Reference>() {
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

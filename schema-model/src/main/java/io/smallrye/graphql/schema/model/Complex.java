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

    private final Comparator referenceComparator = new Comparator<Reference>() {
        @Override
        public int compare(Reference o1, Reference o2) {
            if (o1 != null && o2 != null) {
                return o1.getName().compareTo(o2.getName());
            }
            return ZERO;
        }
    };

    private final Comparator methodComparator = new Comparator<Method>() {
        @Override
        public int compare(Method o1, Method o2) {
            if (o1 != null && o2 != null) {
                return o1.getName().compareTo(o2.getName());
            }
            return ZERO;
        }
    };

    private static final int ZERO = 0;

    private final Set<Method> methods = new TreeSet<>(methodComparator);
    private final Set<Method> sources = new TreeSet<>(methodComparator);
    private final Set<Reference> interfaces = new TreeSet<>(referenceComparator);

    public Complex(String className, String name, ReferenceType type, String description) {
        super(className, name, type, description);
    }

    public Set<Method> getMethods() {
        return methods;
    }

    public void addMethod(Method method) {
        this.methods.add(method);
    }

    public boolean hasMethods() {
        return !this.methods.isEmpty();
    }

    public Set<Method> getSources() {
        return sources;
    }

    public void addSource(Method source) {
        this.sources.add(source);
    }

    public boolean hasSources() {
        return !this.sources.isEmpty();
    }

    public Set<Reference> getInterfaces() {
        return interfaces;
    }

    public void addInterface(Reference interfaceRef) {
        this.interfaces.add(interfaceRef);
    }

    public boolean hasInterfaces() {
        return !this.interfaces.isEmpty();
    }
}

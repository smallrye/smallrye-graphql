package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents a GraphQL Schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Schema implements Serializable {
    private final Set<Complex> queries = newTreeSet();
    private final Set<Complex> mutations = newTreeSet();

    private final Map<String, Complex> inputs = new TreeMap();
    private final Map<String, Complex> types = new TreeMap();
    private final Map<String, Complex> interfaces = new TreeMap();
    private final Map<String, Enum> enums = new TreeMap();

    public Set<Complex> getQueries() {
        return queries;
    }

    public void addQuery(Complex query) {
        this.queries.add(query);
    }

    public boolean hasQueries() {
        return !this.queries.isEmpty();
    }

    public Set<Complex> getMutations() {
        return mutations;
    }

    public void addMutation(Complex mutation) {
        this.mutations.add(mutation);
    }

    public boolean hasMutations() {
        return !this.mutations.isEmpty();
    }

    public Map<String, Complex> getInputs() {
        return inputs;
    }

    public void addInput(Complex input) {
        this.inputs.put(input.getName(), input);
    }

    public boolean containsInput(String name) {
        return this.inputs.containsKey(name);
    }

    public boolean hasInputs() {
        return !this.inputs.isEmpty();
    }

    public Map<String, Complex> getTypes() {
        return types;
    }

    public void addType(Complex type) {
        this.types.put(type.getName(), type);
    }

    public boolean containsType(String name) {
        return this.types.containsKey(name);
    }

    public boolean hasTypes() {
        return !this.types.isEmpty();
    }

    public Map<String, Complex> getInterfaces() {
        return interfaces;
    }

    public void addInterface(Complex interfaceType) {
        this.interfaces.put(interfaceType.getName(), interfaceType);
    }

    public boolean containsInterface(String name) {
        return this.interfaces.containsKey(name);
    }

    public boolean hasInterfaces() {
        return !this.interfaces.isEmpty();
    }

    public Map<String, Enum> getEnums() {
        return enums;
    }

    public void addEnum(Enum enumType) {
        this.enums.put(enumType.getName(), enumType);
    }

    public boolean containsEnum(String name) {
        return this.enums.containsKey(name);
    }

    public boolean hasEnums() {
        return !this.enums.isEmpty();
    }

    private TreeSet<Complex> newTreeSet() {
        return new TreeSet<>(new Comparator<Complex>() {
            @Override
            public int compare(Complex o1, Complex o2) {
                if (o1 != null && o2 != null) {
                    return o1.getClassName().compareTo(o2.getClassName());
                }
                return ZERO;
            }
        });
    }

    private static final int ZERO = 0;
}
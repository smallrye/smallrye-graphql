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
    private Set<Definition> queries;
    private Set<Definition> mutations;

    private Map<String, Definition> inputs;
    private Map<String, Definition> types;
    private Map<String, Definition> interfaces;
    private Map<String, Enum> enums;

    public Set<Definition> getQueries() {
        return queries;
    }

    public void addQuery(Definition query) {
        if (this.queries == null)
            this.queries = newTreeSet();
        this.queries.add(query);
    }

    public Set<Definition> getMutations() {
        return mutations;
    }

    public void addMutation(Definition mutation) {
        if (this.mutations == null)
            this.mutations = newTreeSet();
        this.mutations.add(mutation);
    }

    public Map<String, Definition> getInputs() {
        return inputs;
    }

    public void addInput(Definition input) {
        if (this.inputs == null)
            this.inputs = new TreeMap();
        this.inputs.put(input.getName(), input);
    }

    public boolean containsInput(String name) {
        return this.inputs != null && this.inputs.containsKey(name);
    }

    public Map<String, Definition> getTypes() {
        return types;
    }

    public void addType(Definition type) {
        if (this.types == null)
            this.types = new TreeMap();
        this.types.put(type.getName(), type);
    }

    public boolean containsType(String name) {
        return this.types != null && this.types.containsKey(name);
    }

    public Map<String, Definition> getInterfaces() {
        return interfaces;
    }

    public void addInterface(Definition interfaceType) {
        if (this.interfaces == null)
            this.interfaces = new TreeMap();
        this.interfaces.put(interfaceType.getName(), interfaceType);
    }

    public boolean containsInterface(String name) {
        return this.interfaces != null && this.interfaces.containsKey(name);
    }

    public Map<String, Enum> getEnums() {
        return enums;
    }

    public void addEnum(Enum enumType) {
        if (this.enums == null)
            this.enums = new TreeMap();
        this.enums.put(enumType.getName(), enumType);
    }

    public boolean containsEnum(String name) {
        return this.enums != null && this.enums.containsKey(name);
    }

    private TreeSet<Definition> newTreeSet() {
        return new TreeSet<>(new Comparator<Definition>() {
            @Override
            public int compare(Definition o1, Definition o2) {
                if (o1 != null && o2 != null) {
                    return o1.getClassName().compareTo(o2.getClassName());
                }
                return ZERO;
            }
        });
    }

    private static final int ZERO = 0;
}
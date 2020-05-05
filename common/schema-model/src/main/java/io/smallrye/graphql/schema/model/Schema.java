package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a GraphQL Schema.
 * 
 * This is the end result we are after and the object that will be passed to the
 * implementation to create the actual endpoints and schema.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Schema implements Serializable {
    private Set<Operation> queries = new HashSet<>();
    private Set<Operation> mutations = new HashSet<>();

    private Map<String, InputType> inputs = new HashMap<>();
    private Map<String, Type> types = new HashMap();
    private Map<String, InterfaceType> interfaces = new HashMap();
    private Map<String, EnumType> enums = new HashMap();

    public Schema() {
    }

    public Set<Operation> getQueries() {
        return queries;
    }

    public void setQueries(Set<Operation> queries) {
        this.queries = queries;
    }

    public void addQuery(Operation query) {
        this.queries.add(query);
    }

    public boolean hasQueries() {
        return !this.queries.isEmpty();
    }

    public Set<Operation> getMutations() {
        return mutations;
    }

    public void setMutations(Set<Operation> mutations) {
        this.mutations = mutations;
    }

    public void addMutation(Operation mutation) {
        this.mutations.add(mutation);
    }

    public boolean hasMutations() {
        return !this.mutations.isEmpty();
    }

    public Map<String, InputType> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, InputType> inputs) {
        this.inputs = inputs;
    }

    public void addInput(InputType input) {
        this.inputs.put(input.getName(), input);
    }

    public boolean containsInput(String name) {
        return this.inputs.containsKey(name);
    }

    public boolean hasInputs() {
        return !this.inputs.isEmpty();
    }

    public Map<String, Type> getTypes() {
        return types;
    }

    public void setTypes(Map<String, Type> types) {
        this.types = types;
    }

    public void addType(Type type) {
        this.types.put(type.getName(), type);
    }

    public boolean containsType(String name) {
        return this.types.containsKey(name);
    }

    public boolean hasTypes() {
        return !this.types.isEmpty();
    }

    public Map<String, InterfaceType> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Map<String, InterfaceType> interfaces) {
        this.interfaces = interfaces;
    }

    public void addInterface(InterfaceType interfaceType) {
        this.interfaces.put(interfaceType.getName(), interfaceType);
    }

    public boolean containsInterface(String name) {
        return this.interfaces.containsKey(name);
    }

    public boolean hasInterfaces() {
        return !this.interfaces.isEmpty();
    }

    public Map<String, EnumType> getEnums() {
        return enums;
    }

    public void setEnums(Map<String, EnumType> enums) {
        this.enums = enums;
    }

    public void addEnum(EnumType enumType) {
        this.enums.put(enumType.getName(), enumType);
    }

    public boolean containsEnum(String name) {
        return this.enums.containsKey(name);
    }

    public boolean hasEnums() {
        return !this.enums.isEmpty();
    }

}

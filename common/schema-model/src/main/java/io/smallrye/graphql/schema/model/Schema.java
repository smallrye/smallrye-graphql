package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a GraphQL Schema.
 * <p>
 * This is the end result we are after and the object that will be passed to the
 * implementation to create the actual endpoints and schema.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Schema implements Serializable {
    private Set<Operation> queries = new HashSet<>();
    private Set<Operation> mutations = new HashSet<>();
    private Set<Operation> subscriptions = new HashSet<>();

    private Map<Group, Set<Operation>> groupedQueries = new HashMap<>();
    private Map<Group, Set<Operation>> groupedMutations = new HashMap<>();
    private Map<Group, Set<Operation>> groupedSubscriptions = new HashMap<>();

    private List<DirectiveType> directiveTypes = new ArrayList<>();
    private Map<String, InputType> inputs = new HashMap<>();
    private Map<String, Type> types = new HashMap<>();
    private Map<String, Type> interfaces = new HashMap<>();
    private Map<String, EnumType> enums = new HashMap<>();

    private Map<String, ErrorInfo> errors = new HashMap<>();

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

    public boolean hasOperations() {
        return hasQueries() || hasGroupedQueries() || hasMutations() || hasGroupedMutations();
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

    public Set<Operation> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Set<Operation> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public void addSubscription(Operation subscription) {
        this.subscriptions.add(subscription);
    }

    public boolean hasSubscriptions() {
        return !this.subscriptions.isEmpty();
    }

    public Map<Group, Set<Operation>> getGroupedQueries() {
        return groupedQueries;
    }

    public void setGroupedQueries(Map<Group, Set<Operation>> groupedQueries) {
        this.groupedQueries = groupedQueries;
    }

    public void addGroupedQuery(Group group, Operation query) {
        addToOperationMap(this.groupedQueries, group, query);
    }

    public boolean hasGroupedQueries() {
        return !this.groupedQueries.isEmpty();
    }

    public Map<Group, Set<Operation>> getGroupedMutations() {
        return groupedMutations;
    }

    public void setGroupedMutations(Map<Group, Set<Operation>> groupedMutations) {
        this.groupedMutations = groupedMutations;
    }

    public void addGroupedMutation(Group group, Operation mutation) {
        addToOperationMap(this.groupedMutations, group, mutation);
    }

    public boolean hasGroupedMutations() {
        return !this.groupedMutations.isEmpty();
    }

    public Map<Group, Set<Operation>> getGroupedSubscriptions() {
        return groupedSubscriptions;
    }

    public void setGroupedSubscriptions(Map<Group, Set<Operation>> groupedSubscriptions) {
        this.groupedSubscriptions = groupedSubscriptions;
    }

    public void addGroupedSubscription(Group group, Operation subscription) {
        addToOperationMap(this.groupedSubscriptions, group, subscription);
    }

    public boolean hasGroupedSubscriptions() {
        return !this.groupedSubscriptions.isEmpty();
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

    public Map<String, Type> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(Map<String, Type> interfaces) {
        this.interfaces = interfaces;
    }

    public void addInterface(Type interfaceType) {
        if (interfaceType.getFields() != null && !interfaceType.getFields().isEmpty()) {
            this.interfaces.put(interfaceType.getName(), interfaceType);
        }
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

    public Map<String, ErrorInfo> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, ErrorInfo> errors) {
        this.errors = errors;
    }

    public void addError(ErrorInfo error) {
        this.errors.put(error.getClassName(), error);
    }

    public boolean containsError(String classname) {
        return this.errors.containsKey(classname);
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public List<Operation> getBatchOperations() {
        List<Operation> batchOperations = new ArrayList<>();
        if (hasTypes()) {
            for (Type type : this.types.values()) {
                if (type.hasBatchOperations()) {
                    batchOperations.addAll(type.getBatchOperations().values());
                }
            }
        }
        return batchOperations;
    }

    private void addToOperationMap(Map<Group, Set<Operation>> map, Group group, Operation query) {
        Set<Operation> set;

        if (map.containsKey(group)) {
            set = map.get(group);
        } else {
            set = new HashSet<>();
        }
        set.add(query);
        map.put(group, set);
    }

    public List<DirectiveType> getDirectiveTypes() {
        return directiveTypes;
    }

    public void setDirectiveTypes(List<DirectiveType> directiveTypes) {
        this.directiveTypes = directiveTypes;
    }

    public void addDirectiveType(DirectiveType directiveType) {
        directiveTypes.add(directiveType);
    }

    public boolean hasDirectiveTypes() {
        return !directiveTypes.isEmpty();
    }
}

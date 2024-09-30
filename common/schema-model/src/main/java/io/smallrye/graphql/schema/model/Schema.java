package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a GraphQL Schema.
 * <p>
 * This is the end result we are after and the object that will be passed to the
 * implementation to create the actual endpoints and schema.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Schema implements Serializable {
    private String description;
    private Set<Operation> queries = new HashSet<>();
    private Set<Operation> mutations = new HashSet<>();
    private Set<Operation> subscriptions = new HashSet<>();
    private Set<Operation> resolvers = new HashSet<>();

    private Map<String, NamespaceContainer> namespacedQueries = new HashMap<>();
    private Map<String, NamespaceContainer> namespacedMutations = new HashMap<>();

    private List<CustomScalarType> customScalarTypes = new ArrayList<>();
    private List<DirectiveType> directiveTypes = new ArrayList<>();
    private Map<String, InputType> inputs = new HashMap<>();
    private Map<String, Type> types = new HashMap<>();
    private Map<String, Type> interfaces = new HashMap<>();
    private Map<String, UnionType> unions = new HashMap<>();
    private Map<String, EnumType> enums = new HashMap<>();

    private Map<String, ErrorInfo> errors = new HashMap<>();

    private Map<String, String> wrappedDataFetchers = new HashMap<>();
    private Map<String, String> fieldDataFetchers = new HashMap<>();
    private List<DirectiveInstance> directiveInstances = new ArrayList<>();

    public Schema() {
    }

    public Map<String, NamespaceContainer> getNamespacedQueries() {
        return namespacedQueries;
    }

    public void setNamespacedQueries(Map<String, NamespaceContainer> namespacedQueries) {
        this.namespacedQueries = namespacedQueries;
    }

    public Map<String, NamespaceContainer> getNamespacedMutations() {
        return namespacedMutations;
    }

    public Set<Operation> getAllOperations() {
        Set<Operation> operations = new HashSet<>();
        operations.addAll(queries);
        operations.addAll(mutations);
        operations.addAll(subscriptions);
        operations.addAll(getAllNamespacedQueryOperations());
        operations.addAll(getAllNamespacedMutationOperations());
        return operations;
    }

    public Set<Operation> getAllNamespacedQueryOperations() {
        return namespacedQueries.values().stream()
                .map(NamespaceContainer::getAllOperations)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public Set<Operation> getAllNamespacedMutationOperations() {
        return namespacedMutations.values().stream()
                .map(NamespaceContainer::getAllOperations)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public void setNamespacedMutations(Map<String, NamespaceContainer> namespacedMutations) {
        this.namespacedMutations = namespacedMutations;
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
        return hasQueries() || hasNamespaceQueries()
                || hasMutations() || hasNamespaceMutations()
                || hasSubscriptions() || hasResolvers();
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

    public Set<Operation> getResolvers() {
        return resolvers;
    }

    public void setResolvers(Set<Operation> resolvers) {
        this.resolvers = resolvers;
    }

    public void addResolver(Operation resolver) {
        this.resolvers.add(resolver);
    }

    public boolean hasResolvers() {
        return !this.resolvers.isEmpty();
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

    public Map<String, UnionType> getUnions() {
        return unions;
    }

    public void setUnions(Map<String, UnionType> unions) {
        this.unions = unions;
    }

    public void addUnion(UnionType unionType) {
        this.unions.put(unionType.getName(), unionType);
    }

    public boolean containsUnion(String name) {
        return this.unions.containsKey(name);
    }

    public boolean hasUnions() {
        return !this.unions.isEmpty();
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

    public Map<String, String> getWrappedDataFetchers() {
        return this.wrappedDataFetchers;
    }

    public void setWrappedDataFetchers(Map<String, String> wrappedDataFetchers) {
        this.wrappedDataFetchers = wrappedDataFetchers;
    }

    public void addWrappedDataFetcher(String forReturn, String className) {
        this.wrappedDataFetchers.put(forReturn, className);
    }

    public boolean hasWrappedDataFetchers() {
        return !this.wrappedDataFetchers.isEmpty();
    }

    public Map<String, String> getFieldDataFetchers() {
        return this.fieldDataFetchers;
    }

    public void setFieldDataFetchers(Map<String, String> fieldDataFetchers) {
        this.fieldDataFetchers = fieldDataFetchers;
    }

    public void addFieldDataFetcher(String forReturn, String className) {
        this.fieldDataFetchers.put(forReturn, className);
    }

    public boolean hasFieldDataFetchers() {
        return !this.fieldDataFetchers.isEmpty();
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

    public void addCustomScalarType(CustomScalarType customScalarType) {
        customScalarTypes.add(customScalarType);
        Scalars.registerCustomScalarInSchema(
                customScalarType.getName(),
                customScalarType.getClassName());
    }

    public List<CustomScalarType> getCustomScalarTypes() {
        return customScalarTypes;
    }

    public boolean hasCustomScalarTypes() {
        return !customScalarTypes.isEmpty();
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

    @Override
    public String toString() {
        return "Schema{" +
                "description='" + description + '\'' +
                ", queries=" + queries +
                ", mutations=" + mutations +
                ", subscriptions=" + subscriptions +
                ", resolvers=" + resolvers +
                ", namespacedQueries=" + namespacedQueries +
                ", namespacedMutations=" + namespacedMutations +
                ", directiveTypes=" + directiveTypes +
                ", customScalarTypes=" + customScalarTypes +
                ", inputs=" + inputs +
                ", types=" + types +
                ", interfaces=" + interfaces +
                ", unions=" + unions +
                ", enums=" + enums +
                ", errors=" + errors +
                ", directiveInstances=" + directiveInstances +
                '}';
    }

    public List<DirectiveInstance> getDirectiveInstances() {
        return directiveInstances;
    }

    public void setDirectiveInstances(List<DirectiveInstance> directiveInstances) {
        this.directiveInstances = directiveInstances;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addNamespacedQuery(Namespace namespace, Operation operation) {
        NamespaceContainer groupContainer = namespacedQueries.computeIfAbsent(
                namespace.getNames().get(0),
                key -> new NamespaceContainer());
        groupContainer.add(namespace.getNames(), namespace.getDescription(), operation);
    }

    public void addNamespacedMutation(Namespace namespace, Operation operation) {
        NamespaceContainer groupContainer = namespacedMutations.computeIfAbsent(
                namespace.getNames().get(0),
                key -> new NamespaceContainer());
        groupContainer.add(namespace.getNames(), namespace.getDescription(), operation);
    }

    public boolean hasNamespaceQueries() {
        return namespacedQueries.values().stream().anyMatch(NamespaceContainer::hasOperations);
    }

    public boolean hasNamespaceMutations() {
        return namespacedMutations.values().stream().anyMatch(NamespaceContainer::hasOperations);
    }
}

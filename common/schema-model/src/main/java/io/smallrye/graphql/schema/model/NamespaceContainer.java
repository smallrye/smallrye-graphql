package io.smallrye.graphql.schema.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NamespaceContainer {
    private String name;
    private String description;
    private Set<Operation> operations = new HashSet<>();
    private Map<String, NamespaceContainer> container = new HashMap<>();

    public NamespaceContainer() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Operation> getOperations() {
        return operations;
    }

    public void setOperations(Set<Operation> operations) {
        this.operations = operations;
    }

    public Map<String, NamespaceContainer> getContainer() {
        return container;
    }

    public void setContainer(Map<String, NamespaceContainer> container) {
        this.container = container;
    }

    public boolean hasOperations() {
        return !operations.isEmpty() || container.values().stream().anyMatch(this::hasOperations);
    }

    private boolean hasOperations(NamespaceContainer namespace) {
        return !namespace.getOperations().isEmpty()
                || namespace.container.values().stream().anyMatch(this::hasOperations);
    }

    public void add(Collection<String> names, String description, Operation operation) {
        if (names.isEmpty()) {
            throw new RuntimeException("Namespaces can't be empty");
        }
        ArrayDeque<String> queue = new ArrayDeque<>(names);
        String name = queue.poll();
        add(name, description, queue, operation);
    }

    private void add(String name, String description, ArrayDeque<String> queue, Operation operation) {
        this.name = name;

        if (queue.isEmpty()) {
            this.description = description;
            this.operations.add(operation);
        } else {
            String key = queue.poll();
            NamespaceContainer groupContainer = container.computeIfAbsent(key, s -> new NamespaceContainer());
            groupContainer.add(key, description, queue, operation);
        }
    }

    public List<Operation> getAllOperations() {
        List<Operation> operations = new ArrayList<>(this.operations);
        container.values().forEach(groupContainer -> operations.addAll(groupContainer.getAllOperations()));
        return operations;
    }
}

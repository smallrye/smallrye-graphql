package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GroupContainer implements Serializable {
    private String name;
    private String description;
    private Set<Operation> operations = new HashSet<>();
    private Map<String, GroupContainer> container = new HashMap<>();

    public GroupContainer() {
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

    public Map<String, GroupContainer> getContainer() {
        return container;
    }

    public void setContainer(Map<String, GroupContainer> container) {
        this.container = container;
    }

    public boolean hasOperations() {
        return !operations.isEmpty() || container.values().stream().anyMatch(this::hasOperations);
    }

    private boolean hasOperations(GroupContainer namespace) {
        return !namespace.getOperations().isEmpty()
                || namespace.container.values().stream().anyMatch(this::hasOperations);
    }

    public void add(Collection<String> names, String description, Operation operation) {
        if (names.isEmpty()) {
            throw new IllegalArgumentException("Namespaces cannot be empty");
        }
        Deque<String> queue = new ArrayDeque<>(names);
        String name = queue.poll();
        add(name, description, queue, operation);
    }

    private void add(String name, String description, Deque<String> queue, Operation operation) {
        this.name = name;
        if (queue.isEmpty()) {
            this.description = description;
            this.operations.add(operation);
        } else {
            String key = queue.poll();
            GroupContainer groupContainer = container.computeIfAbsent(key, s -> new GroupContainer());
            groupContainer.add(key, description, queue, operation);
        }
    }

    public List<Operation> getAllOperations() {
        List<Operation> operations = new ArrayList<>(this.operations);

        container.values().forEach(groupContainer -> operations.addAll(groupContainer.getAllOperations()));

        return operations;
    }
}

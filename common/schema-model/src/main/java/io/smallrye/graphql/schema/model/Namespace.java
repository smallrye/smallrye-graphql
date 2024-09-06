package io.smallrye.graphql.schema.model;

import java.util.List;
import java.util.Objects;

public class Namespace {
    private List<String> names;
    private String description;

    public Namespace() {
    }

    public List<String> getNames() {
        return names;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Namespace namespace = (Namespace) o;
        return Objects.equals(names, namespace.names) && Objects.equals(description, namespace.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(names, description);
    }

    @Override
    public String toString() {
        return "Namespace{" + "names=" + names + ", description=" + description + '}';
    }
}

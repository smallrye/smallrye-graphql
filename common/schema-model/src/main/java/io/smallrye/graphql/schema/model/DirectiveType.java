package io.smallrye.graphql.schema.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A custom directive in the Schema, i.e. the thing that gets declared in the SDL.
 * When applied somewhere, it's a {@link DirectiveInstance}.
 *
 * @see <a href="https://spec.graphql.org/draft/#sec-Type-System.Directives.Custom-Directives">Custom Directive</a>
 */
public final class DirectiveType {
    private String className;
    private String name;
    private String description;
    private Set<String> locations = new LinkedHashSet<>();
    private final Map<String, DirectiveArgument> argumentTypes = new LinkedHashMap<>();

    public DirectiveType() {
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setLocations(Set<String> locations) {
        this.locations = locations;
    }

    public Set<String> getLocations() {
        return this.locations;
    }

    public Set<String> getArgumentNames() {
        return this.argumentTypes.keySet();
    }

    public DirectiveArgument getArgumentType(String name) {
        return this.argumentTypes.get(name);
    }

    public void addArgumentType(DirectiveArgument type) {
        this.argumentTypes.put(type.getName(), type);
    }

    @Override
    public String toString() {
        return "DirectiveType(" +
                ((className == null) ? "" : "className='" + className + '\'') +
                ((name == null) ? "" : ", name='" + name + '\'') +
                ((description == null) ? "" : ", description='" + description + '\'') +
                ((locations == null) ? "" : ", locations=" + locations) +
                ((argumentTypes.isEmpty()) ? "" : ", argumentTypes=" + argumentTypes) +
                ")";
    }
}

package io.smallrye.graphql.schema.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A custom directive in the Schema, i.e. the thing that gets declared in the SDL.
 * When applied somewhere, it's a {@link DirectiveInstance}.
 *
 * @see <a href="https://spec.graphql.org/draft/#sec-Type-System.Directives.Custom-Directives">Custom Directive</a>
 */
public class DirectiveType {
    private String className;
    private String name;
    private String description;
    private Set<String> locations = new LinkedHashSet<>();
    private List<DirectiveArgument> argumentTypes = new ArrayList<>();
    private boolean repeatable;

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

    public List<DirectiveArgument> getArgumentTypes() {
        return argumentTypes;
    }

    public void setArgumentTypes(List<DirectiveArgument> argumentTypes) {
        this.argumentTypes = argumentTypes;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    /**
     * Helper 'getter' methods, but DON'T add 'get' into their names, otherwise it breaks Quarkus bytecode recording,
     * because they would be detected as actual property getters while they are actually not
     */

    public Map<String, DirectiveArgument> argumentTypesAsMap() {
        return argumentTypes.stream().collect(Collectors.toMap(Field::getName, arg -> arg));
    }

    public Set<String> argumentNames() {
        return this.argumentTypesAsMap().keySet();
    }

    public DirectiveArgument argumentType(String name) {
        return this.argumentTypesAsMap().get(name);
    }

    public void addArgumentType(DirectiveArgument type) {
        this.argumentTypes.add(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        DirectiveType that = (DirectiveType) o;
        return repeatable == that.repeatable && Objects.equals(className, that.className) && Objects.equals(name, that.name)
                && Objects.equals(description, that.description) && Objects.equals(locations, that.locations)
                && Objects.equals(argumentTypes, that.argumentTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, name, description, locations, argumentTypes, repeatable);
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

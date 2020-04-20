package io.smallrye.graphql.schema.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Represent an operation on a Query or Mutation,
 * or an operation on a Type in the case of a Source annotation.
 * 
 * a Operation if a special kind on field that allows arguments.
 * 
 * @see <a href="https://spec.graphql.org/draft/#sec-The-__Field-Type">Field</a>
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Operation extends Field {
    private String className; // Java class this is on

    private List<Argument> arguments = new LinkedList<>();

    public Operation() {
    }

    public Operation(String className, String methodName, String propertyName, String name, String description,
            Reference reference) {
        super(methodName, propertyName, name, description, reference);
        this.className = className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public List<Argument> getArguments() {
        return this.arguments;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public void addArgument(Argument argument) {
        this.arguments.add(argument);
    }

    public boolean hasArguments() {
        return !this.arguments.isEmpty();
    }
}
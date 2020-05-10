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

    private OperationType operationType;

    private Reference containingType;

    private boolean async;

    public Operation() {
    }

    public Operation(String className, String methodName, String propertyName, String name, String description,
            Reference reference, final OperationType operationType) {
        super(methodName, propertyName, name, description, reference);
        this.className = className;
        this.operationType = operationType;
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

    public void setOperationType(final OperationType operationType) {
        this.operationType = operationType;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public Reference getContainingType() {
        return containingType;
    }

    public void setContainingType(final Reference containingType) {
        this.containingType = containingType;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(final boolean async) {
        this.async = async;
    }
}

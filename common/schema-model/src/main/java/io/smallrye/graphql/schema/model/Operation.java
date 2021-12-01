package io.smallrye.graphql.schema.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Represent an operation on a Query or Mutation,
 * or an operation on a Type in the case of a Source annotation.
 * <p>
 * a Operation if a special kind on field that allows arguments.
 *
 * @see <a href="https://spec.graphql.org/draft/#sec-The-__Field-Type">Field</a>
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Operation extends Field {
    /**
     * Java class this is on
     */
    private String className;

    /**
     * The arguments (if any)
     */
    private List<Argument> arguments = new LinkedList<>();

    /**
     * Operation Type (Query/Mutation)
     */
    private OperationType operationType;

    /**
     * If this is a source fields, the object it's on
     */
    private Reference sourceFieldOn = null;

    public Operation() {
    }

    public Operation(String className, String methodName, String propertyName, String name, Reference reference,
            final OperationType operationType) {
        super(methodName, propertyName, name, reference);
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

    public Reference getSourceFieldOn() {
        return sourceFieldOn;
    }

    public void setSourceFieldOn(Reference sourceFieldOn) {
        this.sourceFieldOn = sourceFieldOn;
    }

    public boolean isSourceField() {
        return this.sourceFieldOn != null;
    }

    @Override
    public String toString() {
        return "Operation{" + "className=" + className + ", arguments=" + arguments + ", operationType=" + operationType
                + ", sourceFieldOn=" + sourceFieldOn + '}';
    }
}

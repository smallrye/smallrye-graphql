package io.smallrye.graphql.schema.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a field on a type/input/interface
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Method extends Item {
    private Field returnField;
    private final List<Field> parameters = new LinkedList<>();

    public Method(String name, String description, String javaName) {
        super(name, description, javaName);
    }

    public Field getReturn() {
        return returnField;
    }

    public void setReturn(Field returnField) {
        this.returnField = returnField;
    }

    public List<Field> getParameters() {
        return parameters;
    }

    public void addParameter(Field parameter) {
        this.parameters.add(parameter);
    }

    public boolean hasParameters() {
        return !this.parameters.isEmpty();
    }
}
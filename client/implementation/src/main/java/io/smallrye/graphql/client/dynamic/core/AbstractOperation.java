package io.smallrye.graphql.client.dynamic.core;

import java.util.List;

import io.smallrye.graphql.client.mpapi.core.Field;
import io.smallrye.graphql.client.mpapi.core.Operation;
import io.smallrye.graphql.client.mpapi.core.OperationType;
import io.smallrye.graphql.client.mpapi.core.Variable;

public abstract class AbstractOperation implements Operation {
    private OperationType type;
    private String name;
    private List<Variable> variables;
    private List<Field> fields;

    /*
     * Constructors
     */
    public AbstractOperation() {
    }

    /*
     * Getter/Setter
     */
    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void setVariables(List<Variable> vars) {
        this.variables = vars;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}

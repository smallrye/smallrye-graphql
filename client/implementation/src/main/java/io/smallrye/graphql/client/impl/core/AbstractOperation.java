package io.smallrye.graphql.client.impl.core;

import java.util.List;

import io.smallrye.graphql.client.core.Directive;
import io.smallrye.graphql.client.core.FieldOrFragment;
import io.smallrye.graphql.client.core.Operation;
import io.smallrye.graphql.client.core.OperationType;
import io.smallrye.graphql.client.core.Variable;

public abstract class AbstractOperation implements Operation {
    private OperationType type;
    private String name;
    private List<Variable> variables;
    private List<FieldOrFragment> fields;
    private List<Directive> directives;

    /*
     * Constructors
     */
    public AbstractOperation() {
    }

    /*
     * Getter/Setter
     */
    @Override
    public OperationType getType() {
        return type;
    }

    @Override
    public void setType(OperationType type) {
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<Variable> getVariables() {
        return variables;
    }

    @Override
    public void setVariables(List<Variable> vars) {
        this.variables = vars;
    }

    @Override
    public List<FieldOrFragment> getFields() {
        return fields;
    }

    @Override
    public void setFields(List<FieldOrFragment> fields) {
        this.fields = fields;
    }

    @Override
    public List<Directive> getDirectives() {
        return directives;
    }

    @Override
    public void setDirectives(List<Directive> directives) {
        this.directives = directives;
    }
}

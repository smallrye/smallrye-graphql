package io.smallrye.graphql.client.impl.core;

import java.util.Optional;

import io.smallrye.graphql.client.core.Variable;
import io.smallrye.graphql.client.core.VariableType;

public abstract class AbstractVariable implements Variable {
    private String name;
    private VariableType type;
    private Optional<Object> defaultValue;

    /*
     * Constructors
     */
    public AbstractVariable() {
    }

    /*
     * Getter/Setter
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public VariableType getType() {
        return type;
    }

    @Override
    public void setType(VariableType type) {
        this.type = type;
    }

    @Override
    public Optional<Object> getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void setDefaultValue(Optional<Object> value) {
        this.defaultValue = value;
    }
}

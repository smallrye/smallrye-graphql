package io.smallrye.graphql.client.impl.core;

import io.smallrye.graphql.client.core.VariableType;

public abstract class AbstractVariableType implements VariableType {
    private String name;
    private boolean nonNull;
    private VariableType child;

    /*
     * Constructors
     */
    public AbstractVariableType() {
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
    public boolean isNonNull() {
        return nonNull;
    }

    @Override
    public void setNonNull(boolean nonNull) {
        this.nonNull = nonNull;
    }

    @Override
    public VariableType getChild() {
        return child;
    }

    @Override
    public void setChild(VariableType child) {
        this.child = child;
    }
}

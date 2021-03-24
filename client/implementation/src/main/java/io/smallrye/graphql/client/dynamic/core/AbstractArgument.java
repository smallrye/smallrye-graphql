package io.smallrye.graphql.client.dynamic.core;

import io.smallrye.graphql.client.core.Argument;

public abstract class AbstractArgument implements Argument {
    private String name;
    private Object value;

    public AbstractArgument() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}

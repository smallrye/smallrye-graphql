package io.smallrye.graphql.client.impl.core;

import io.smallrye.graphql.client.core.DirectiveArgument;

public abstract class AbstractDirectiveArgument implements DirectiveArgument {
    private String name;
    private Object value;

    public AbstractDirectiveArgument() {
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

package io.smallrye.graphql.client.impl.core;

import static io.smallrye.graphql.client.core.utils.validation.NameValidation.validateName;

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
        this.name = validateName(name);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}

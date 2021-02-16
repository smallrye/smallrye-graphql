package io.smallrye.graphql.client.dynamic.core;

import io.smallrye.graphql.client.mpapi.core.Enum;

public abstract class AbstractEnum implements Enum {

    private String value;

    public AbstractEnum() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

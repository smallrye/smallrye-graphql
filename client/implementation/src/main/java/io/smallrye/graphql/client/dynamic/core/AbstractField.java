package io.smallrye.graphql.client.dynamic.core;

import java.util.List;

import io.smallrye.graphql.client.mpapi.core.Argument;
import io.smallrye.graphql.client.mpapi.core.Field;

public abstract class AbstractField implements Field {
    private String name;
    private List<Argument> arguments;
    private List<Field> fields;

    public AbstractField() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}

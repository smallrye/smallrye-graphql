package io.smallrye.graphql.client.dynamic.core;

import java.util.List;

import io.smallrye.graphql.client.core.Field;
import io.smallrye.graphql.client.core.Fragment;

public abstract class AbstractFragment implements Fragment {

    private String name;
    private String targetType;
    private List<Field> fields;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<Field> getFields() {
        return fields;
    }

    @Override
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public String getTargetType() {
        return targetType;
    }

    @Override
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
}

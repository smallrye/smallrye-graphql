package io.smallrye.graphql.client.impl.core;

import java.util.List;

import io.smallrye.graphql.client.core.FieldOrFragment;
import io.smallrye.graphql.client.core.Fragment;

public abstract class AbstractFragment implements Fragment {

    private String name;
    private String targetType;
    private List<FieldOrFragment> fields;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
    public String getTargetType() {
        return targetType;
    }

    @Override
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
}

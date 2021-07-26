package io.smallrye.graphql.client.dynamic.core;

import java.util.List;

import io.smallrye.graphql.client.core.Field;
import io.smallrye.graphql.client.core.InlineFragment;

public abstract class AbstractInlineFragment implements InlineFragment {

    private String type;
    private List<Field> fields;

    @Override
    public List<Field> getFields() {
        return this.fields;
    }

    @Override
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}

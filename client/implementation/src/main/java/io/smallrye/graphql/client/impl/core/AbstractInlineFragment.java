package io.smallrye.graphql.client.impl.core;

import java.util.List;

import io.smallrye.graphql.client.core.Directive;
import io.smallrye.graphql.client.core.FieldOrFragment;
import io.smallrye.graphql.client.core.InlineFragment;

public abstract class AbstractInlineFragment implements InlineFragment {

    private String type;
    private List<FieldOrFragment> fields;
    private List<Directive> directives;

    @Override
    public List<FieldOrFragment> getFields() {
        return this.fields;
    }

    @Override
    public void setFields(List<FieldOrFragment> fields) {
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

    @Override
    public List<Directive> getDirectives() {
        return directives;
    }

    @Override
    public void setDirectives(List<Directive> directives) {
        this.directives = directives;
    }
}

package io.smallrye.graphql.client.impl.core;

import static io.smallrye.graphql.client.core.utils.validation.NameValidation.validateFragmentName;
import static io.smallrye.graphql.client.core.utils.validation.NameValidation.validateName;

import java.util.List;

import io.smallrye.graphql.client.core.Directive;
import io.smallrye.graphql.client.core.FieldOrFragment;
import io.smallrye.graphql.client.core.Fragment;

public abstract class AbstractFragment implements Fragment {

    private String name;
    private String targetType;
    private List<Directive> directives;
    private List<FieldOrFragment> fields;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = validateFragmentName(name);
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
        this.targetType = validateName(targetType);
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

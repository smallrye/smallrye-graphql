package io.smallrye.graphql.client.impl.core;

import static io.smallrye.graphql.client.core.utils.validation.NameValidation.validateFieldName;

import java.util.List;

import io.smallrye.graphql.client.core.Argument;
import io.smallrye.graphql.client.core.Directive;
import io.smallrye.graphql.client.core.Field;
import io.smallrye.graphql.client.core.FieldOrFragment;

public abstract class AbstractField implements Field {
    private String name;
    private List<Argument> arguments;
    private List<FieldOrFragment> fields;
    private List<Directive> directives;

    public AbstractField() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = validateFieldName(name);
    }

    public List<Argument> getArguments() {
        return arguments;
    }

    public void setArguments(List<Argument> arguments) {
        this.arguments = arguments;
    }

    public List<FieldOrFragment> getFields() {
        return fields;
    }

    public void setFields(List<FieldOrFragment> fields) {
        this.fields = fields;
    }

    public List<Directive> getDirectives() {
        return directives;
    }

    public void setDirectives(List<Directive> directives) {
        this.directives = directives;
    }
}

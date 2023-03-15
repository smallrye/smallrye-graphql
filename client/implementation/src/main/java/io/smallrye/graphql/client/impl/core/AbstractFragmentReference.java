package io.smallrye.graphql.client.impl.core;

import static io.smallrye.graphql.client.core.utils.validation.NameValidation.validateFragmentName;

import java.util.List;

import io.smallrye.graphql.client.core.Directive;
import io.smallrye.graphql.client.core.FragmentReference;

public abstract class AbstractFragmentReference implements FragmentReference {

    private String name;
    private List<Directive> directives;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = validateFragmentName(name);
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

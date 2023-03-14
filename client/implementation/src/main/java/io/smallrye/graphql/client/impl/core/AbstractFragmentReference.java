package io.smallrye.graphql.client.impl.core;

import static io.smallrye.graphql.client.core.utils.validation.NameValidation.validateFragmentName;

import io.smallrye.graphql.client.core.FragmentReference;

public abstract class AbstractFragmentReference implements FragmentReference {

    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = validateFragmentName(name);
    }

}

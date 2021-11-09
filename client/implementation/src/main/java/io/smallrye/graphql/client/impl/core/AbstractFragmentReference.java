package io.smallrye.graphql.client.impl.core;

import io.smallrye.graphql.client.core.FragmentReference;

public abstract class AbstractFragmentReference implements FragmentReference {

    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

}

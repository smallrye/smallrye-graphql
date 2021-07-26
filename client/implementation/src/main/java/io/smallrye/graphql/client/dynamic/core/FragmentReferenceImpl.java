package io.smallrye.graphql.client.dynamic.core;

import io.smallrye.graphql.client.core.exceptions.BuildException;

public class FragmentReferenceImpl extends AbstractFragmentReference {

    @Override
    public String build() throws BuildException {
        return "..." + getName();
    }

}

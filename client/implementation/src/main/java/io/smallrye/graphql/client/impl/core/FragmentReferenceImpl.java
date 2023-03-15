package io.smallrye.graphql.client.impl.core;

import java.util.stream.Collectors;

import io.smallrye.graphql.client.core.Buildable;
import io.smallrye.graphql.client.core.exceptions.BuildException;

public class FragmentReferenceImpl extends AbstractFragmentReference {

    @Override
    public String build() throws BuildException {
        StringBuilder builder = new StringBuilder();
        builder.append("...")
                .append(getName())
                .append(getDirectives()
                        .stream()
                        .map(Buildable::build)
                        .collect(Collectors.joining()));
        return builder.toString();
    }

}

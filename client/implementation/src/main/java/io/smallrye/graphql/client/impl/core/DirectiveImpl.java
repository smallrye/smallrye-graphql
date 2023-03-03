package io.smallrye.graphql.client.impl.core;

import java.util.stream.Collectors;

import io.smallrye.graphql.client.core.Buildable;

public class DirectiveImpl extends AbstractDirective {
    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append(" @") // space at the beginning for chaining multiple directives.
                .append(this.getName());

        if (!getDirectiveArguments().isEmpty()) {
            builder
                    .append("(")
                    .append(_buildDirectiveArgs())
                    .append(")");
        }

        return builder.toString();
    }

    private String _buildDirectiveArgs() {
        return getDirectiveArguments().stream().map(Buildable::build).collect(Collectors.joining(", "));
    }
}

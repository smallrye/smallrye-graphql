package io.smallrye.graphql.client.impl.core;

import static io.smallrye.graphql.client.impl.core.utils.ValueFormatter.format;

import java.util.stream.Collectors;

import io.smallrye.graphql.client.core.Buildable;

public class VariableImpl extends AbstractVariable {
    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();

        builder.append("$");
        builder.append(this.getName());
        builder.append(":");
        builder.append(this.getType().build());

        if (this.getDefaultValue().isPresent()) {
            builder.append("=");
            builder.append(format(this.getDefaultValue().get()));
        }
        builder.append(getDirectives()
                .stream()
                .map(Buildable::build)
                .collect(Collectors.joining()));
        return builder.toString();
    }
}

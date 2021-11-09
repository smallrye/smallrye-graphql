package io.smallrye.graphql.client.impl.core;

import static io.smallrye.graphql.client.impl.core.utils.ValueFormatter.format;

public class VariableImpl extends AbstractVariable {
    @Override
    // TODO: User StringJoiner
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

        return builder.toString();
    }
}

package io.smallrye.graphql.client.dynamic.core;

import static io.smallrye.graphql.client.dynamic.core.utils.ValueFormatter.format;

public class ArgumentImpl extends AbstractArgument {

    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getName());
        builder.append(":");
        builder.append(format(this.getValue()));
        return builder.toString();
    }

}

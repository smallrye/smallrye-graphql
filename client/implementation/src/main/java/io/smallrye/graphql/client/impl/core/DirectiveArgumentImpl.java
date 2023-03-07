package io.smallrye.graphql.client.impl.core;

import static io.smallrye.graphql.client.impl.core.utils.ValueFormatter.format;

public class DirectiveArgumentImpl extends AbstractDirectiveArgument {
    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.getName());
        builder.append(":");
        builder.append(format(this.getValue()));
        return builder.toString();
    }

}

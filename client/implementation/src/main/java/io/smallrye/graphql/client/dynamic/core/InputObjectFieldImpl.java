package io.smallrye.graphql.client.dynamic.core;

import io.smallrye.graphql.client.dynamic.core.utils.ValueFormatter;

public class InputObjectFieldImpl extends AbstractInputObjectField {

    // TODO: Use simple String
    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();

        builder.append(this.getName());
        builder.append(":");
        builder.append(ValueFormatter.format(this.getValue()));

        return builder.toString();
    }

}

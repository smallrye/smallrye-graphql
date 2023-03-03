package io.smallrye.graphql.client.impl.core;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import io.smallrye.graphql.client.core.Buildable;
import io.smallrye.graphql.client.core.exceptions.BuildException;

public class OperationImpl extends AbstractOperation {
    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();

        switch (this.getType()) {
            case QUERY:
                builder.append("query");
                break;
            case MUTATION:
                builder.append("mutation");
                break;
            case SUBSCRIPTION:
                builder.append("subscription");
                break;
            default:
                throw new BuildException("Operation type must be one of QUERY, MUTATION or SUBSCRIPTION");
        }

        builder.append(" ");

        builder.append(this.getName());

        if (!this.getVariables().isEmpty()) {
            _buildVariables(builder);
        }

        if (!this.getDirectives().isEmpty()) {
            _buildDirectives(builder);
        }

        if (!this.getFields().isEmpty()) {
            _buildFields(builder);
        } else {
            throw new BuildException("An operation must have at least one root field.");
        }

        return builder.toString();
    }

    private void _buildVariables(StringBuilder builder) {
        buildWrapper(builder, this::getVariables, ", ", "(", ")");
    }

    private void _buildDirectives(StringBuilder builder) {
        String directives = getDirectives().stream().map(Buildable::build).collect(Collectors.joining());
        /*
         * in case there is a whitespace (30th line) as a last character in the StringBuilder,
         * the string containing all directives will be trimmed, so there won't be any
         * two whitespace characters next to each other.
         */
        if (String.valueOf(builder.charAt(builder.length() - 1)).equals(" "))
            directives = directives.trim();
        builder.append(directives);
    }

    private void _buildFields(StringBuilder builder) {
        buildWrapper(builder, this::getFields, " ", "{", "}");
    }

    private void buildWrapper(StringBuilder builder,
            Supplier<List<?>> getMethod,
            String delimiter,
            String prefix,
            String suffix) {
        StringJoiner stringJoiner = new StringJoiner(delimiter, prefix, suffix);
        getMethod.get().forEach(buildableObject -> stringJoiner.add(((Buildable) buildableObject).build()));
        builder.append(stringJoiner);
    }
}

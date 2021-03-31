package io.smallrye.graphql.client.dynamic.core;

public class FieldImpl extends AbstractField {
    // TODO: Use StringJoiner
    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();

        builder.append(this.getName());

        // Arguments to build ?
        if (!this.getArguments().isEmpty()) {
            builder.append("(");
            builder.append(_buildArgs());
            builder.append(")");
        }

        // Sub-fields to build ?
        if (!this.getFields().isEmpty()) {
            builder.append("{");
            builder.append(_buildFields());
            builder.append("}");
        }

        return builder.toString();
    }

    // TODO: Use StringJoiner  or Stream + Collectors.joining (https://www.baeldung.com/java-strings-concatenation)
    private String _buildArgs() {
        StringBuilder builder = new StringBuilder();

        ArgumentImpl[] arguments = this.getArguments().toArray(new ArgumentImpl[0]);
        for (int i = 0; i < arguments.length; i++) {
            ArgumentImpl argument = arguments[i];
            builder.append(argument.build());
            if (i < arguments.length - 1) {
                builder.append(", ");
            }
        }

        return builder.toString();
    }

    // TODO: Use StringJoiner  or Stream + Collectors.joining (https://www.baeldung.com/java-strings-concatenation)
    private String _buildFields() {
        StringBuilder builder = new StringBuilder();

        FieldImpl[] fields = this.getFields().toArray(new FieldImpl[0]);
        for (int i = 0; i < fields.length; i++) {
            FieldImpl field = fields[i];
            builder.append(field.build());
            if (i < fields.length - 1) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }
}

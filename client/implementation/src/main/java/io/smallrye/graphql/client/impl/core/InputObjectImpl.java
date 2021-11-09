package io.smallrye.graphql.client.impl.core;

public class InputObjectImpl extends AbstractInputObject {

    // TODO: Use StringJoiner  or Stream + Collectors.joining (https://www.baeldung.com/java-strings-concatenation)
    @Override
    public String build() {
        StringBuilder builder = new StringBuilder();

        builder.append("{");
        InputObjectFieldImpl[] inputObjectFields = this.getInputObjectFields().toArray(new InputObjectFieldImpl[0]);
        for (int i = 0; i < inputObjectFields.length; i++) {
            InputObjectFieldImpl inputObjectField = inputObjectFields[i];
            builder.append(inputObjectField.build());
            if (i < inputObjectFields.length - 1) {
                builder.append(" ");
            }
        }
        builder.append("}");

        return builder.toString();
    }

}

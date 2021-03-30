package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceOf;

public interface InputObjectField extends Buildable {

    /*
     * Static factory methods
     */
    // (name, value)
    static InputObjectField prop(String name, Object value) {
        InputObjectField inputObjectField = getNewInstanceOf(InputObjectField.class);

        inputObjectField.setName(name);
        inputObjectField.setValue(value);

        return inputObjectField;
    }

    // (name, variable)
    static InputObjectField prop(String name, Variable var) {
        InputObjectField inputObjectField = getNewInstanceOf(InputObjectField.class);

        inputObjectField.setName(name);
        inputObjectField.setValue(var);

        return inputObjectField;
    }

    /*
     * Getter/Setter
     */
    String getName();

    void setName(String name);

    Object getValue();

    void setValue(Object value);
}

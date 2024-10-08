package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceFromFactory;

import io.smallrye.graphql.client.core.factory.InputObjectFieldFactory;

public interface InputObjectField extends Buildable {

    /*
     * Static factory methods
     */
    // (name, value)
    static InputObjectField prop(String name, Object value) {
        InputObjectField inputObjectField = getNewInstanceFromFactory(InputObjectFieldFactory.class);

        inputObjectField.setName(name);
        inputObjectField.setValue(value);

        return inputObjectField;
    }

    // (name, variable)
    static InputObjectField prop(String name, Variable var) {
        InputObjectField inputObjectField = getNewInstanceFromFactory(InputObjectFieldFactory.class);

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

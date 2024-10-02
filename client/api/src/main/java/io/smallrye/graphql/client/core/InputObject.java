package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceFromFactory;
import static java.util.Arrays.asList;

import java.util.List;

import io.smallrye.graphql.client.core.factory.InputObjectFactory;

public interface InputObject extends Buildable {
    /*
     * Static factory methods
     */
    static InputObject inputObject(InputObjectField... inputObjectFields) {
        InputObject inputObject = getNewInstanceFromFactory(InputObjectFactory.class);

        inputObject.setInputObjectFields(asList(inputObjectFields));

        return inputObject;
    }

    /*
     * Getter/Setter
     */
    List<InputObjectField> getInputObjectFields();

    void setInputObjectFields(List<InputObjectField> inputObjectFields);
}

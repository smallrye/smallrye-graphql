package io.smallrye.graphql.client.mpapi.core;

import static io.smallrye.graphql.client.mpapi.core.utils.ServiceUtils.getNewInstanceOf;
import static java.util.Arrays.asList;

import java.util.List;

public interface InputObject extends Buildable {
    /*
     * Static factory methods
     */
    static InputObject inputObject(InputObjectField... inputObjectFields) {
        InputObject inputObject = getNewInstanceOf(InputObject.class);

        inputObject.setInputObjectFields(asList(inputObjectFields));

        return inputObject;
    }

    /*
     * Getter/Setter
     */
    List<InputObjectField> getInputObjectFields();

    void setInputObjectFields(List<InputObjectField> inputObjectFields);
}

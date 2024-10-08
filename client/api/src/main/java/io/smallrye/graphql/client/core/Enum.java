package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceFromFactory;

import io.smallrye.graphql.client.core.factory.EnumFactory;

public interface Enum extends Buildable {
    /*
     * Static factory methods
     */
    static Enum gqlEnum(String value) {
        Enum gqlEnum = getNewInstanceFromFactory(EnumFactory.class);

        gqlEnum.setValue(value);

        return gqlEnum;
    }

    /*
     * Getter/Setter
     */
    String getValue();

    void setValue(String value);
}

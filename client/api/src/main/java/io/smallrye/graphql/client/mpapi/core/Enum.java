package io.smallrye.graphql.client.mpapi.core;

import static io.smallrye.graphql.client.mpapi.core.utils.ServiceUtils.getNewInstanceOf;

public interface Enum extends Buildable {
    /*
     * Static factory methods
     */
    static Enum gqlEnum(String value) {
        Enum gqlEnum = getNewInstanceOf(Enum.class);

        gqlEnum.setValue(value);

        return gqlEnum;
    }

    /*
     * Getter/Setter
     */
    String getValue();

    void setValue(String value);
}

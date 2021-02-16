package io.smallrye.graphql.client.mpapi.core;

import static io.smallrye.graphql.client.mpapi.core.utils.ServiceUtils.getNewInstanceOf;
import static java.util.Arrays.asList;

import java.util.List;

public interface Argument extends Buildable {
    /*
     * Static factory methods
     */
    static List<Argument> args(Argument... args) {
        return asList(args);
    }

    // (name, raw value)
    static Argument arg(String name, Object value) {
        Argument argument = getNewInstanceOf(Argument.class);

        argument.setName(name);
        argument.setValue(value);

        return argument;
    }

    // (name, inputObject)
    static Argument arg(String name, InputObject inputObject) {
        Argument argument = getNewInstanceOf(Argument.class);

        argument.setName(name);
        argument.setValue(inputObject);

        return argument;
    }

    // (name, variable)
    static Argument arg(String name, Variable var) {
        Argument argument = getNewInstanceOf(Argument.class);

        argument.setName(name);
        argument.setValue(var);

        return argument;
    }

    /*
     * Getter/Setter
     */
    String getName();

    void setName(String name1);

    Object getValue();

    void setValue(Object value);
}

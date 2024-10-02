package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceFromFactory;
import static java.util.Arrays.asList;

import java.util.List;

import io.smallrye.graphql.client.core.factory.ArgumentFactory;

public interface Argument extends Buildable {
    /*
     * Static factory methods
     */
    static List<Argument> args(Argument... args) {
        return asList(args);
    }

    // (name, raw value)
    static Argument arg(String name, Object value) {
        Argument argument = getNewInstanceFromFactory(ArgumentFactory.class);

        argument.setName(name);
        argument.setValue(value);

        return argument;
    }

    // (name, inputObject)
    static Argument arg(String name, InputObject inputObject) {
        Argument argument = getNewInstanceFromFactory(ArgumentFactory.class);

        argument.setName(name);
        argument.setValue(inputObject);

        return argument;
    }

    // (name, variable)
    static Argument arg(String name, Variable var) {
        Argument argument = getNewInstanceFromFactory(ArgumentFactory.class);

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

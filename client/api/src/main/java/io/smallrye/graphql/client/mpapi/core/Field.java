package io.smallrye.graphql.client.mpapi.core;

import static io.smallrye.graphql.client.mpapi.core.utils.ServiceUtils.getNewInstanceOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

public interface Field extends Buildable {
    /*
     * Static factory methods
     */
    static List<Field> fields(Field... fields) {
        return asList(fields);
    }

    // (name)
    static Field field(String name) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(emptyList());
        field.setFields(emptyList());

        return field;
    }

    // (name, subfields)
    static Field field(String name, Field... fields) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(emptyList());
        field.setFields(asList(fields));

        return field;
    }

    // (name, args)
    static Field field(String name, Argument... args) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(asList(args));
        field.setFields(emptyList());

        return field;
    }

    // (name, args, subfields)
    static Field field(String name, List<Argument> args, Field... fields) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(args);
        field.setFields(asList(fields));

        return field;
    }

    /*
     * Getter/Setter
     */
    String getName();

    void setName(String name);

    List<Argument> getArguments();

    void setArguments(List<Argument> arguments);

    List<Field> getFields();

    void setFields(List<Field> fields);
}

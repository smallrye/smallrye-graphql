package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

public interface Field extends FieldOrFragment {
    /*
     * Static factory methods
     */
    static List<FieldOrFragment> fields(FieldOrFragment... fields) {
        return asList(fields);
    }

    // (name)
    static Field field(String name) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(emptyList());
        field.setFields(emptyList());
        field.setDirectives(emptyList());

        return field;
    }

    // (name, subfields)
    static Field field(String name, FieldOrFragment... fields) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(emptyList());
        field.setFields(asList(fields));
        field.setDirectives(emptyList());

        return field;
    }

    // (name, args)
    static Field field(String name, Argument... args) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(asList(args));
        field.setFields(emptyList());
        field.setDirectives(emptyList());

        return field;
    }

    // (name, directives)
    static Field fieldWithDirectives(String name, Directive... directives) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(emptyList());
        field.setFields(emptyList());
        field.setDirectives(asList(directives));

        return field;
    }

    // (name, args, subfields)
    static Field field(String name, List<Argument> args, FieldOrFragment... fields) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(args);
        field.setFields(asList(fields));
        field.setDirectives(emptyList());

        return field;
    }

    // (name, args, directives)
    static Field fieldWithDirectives(String name, List<Argument> args, List<Directive> directives) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(args);
        field.setDirectives(directives);
        field.setFields(emptyList());

        return field;
    }

    // (name, directives, fields)
    static Field fieldWithDirectives(String name, List<Directive> directives, FieldOrFragment... fields) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(emptyList());
        field.setDirectives(directives);
        field.setFields(asList(fields));

        return field;
    }

    // (name, args, directives, subfields)
    static Field fieldWithDirectives(String name, List<Argument> args, List<Directive> directives, FieldOrFragment... fields) {
        Field field = getNewInstanceOf(Field.class);

        field.setName(name);
        field.setArguments(args);
        field.setDirectives(directives);
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

    List<FieldOrFragment> getFields();

    void setFields(List<FieldOrFragment> fields);

    List<Directive> getDirectives();

    void setDirectives(List<Directive> directives);
}

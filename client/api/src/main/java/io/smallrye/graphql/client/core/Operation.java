package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.OperationType.QUERY;
import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

public interface Operation extends FragmentOrOperation {
    /*
     * Helpers
     */
    static List<Operation> operations(Operation... operations) {
        return asList(operations);
    }

    // (fields)
    static Operation operation(FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName("");
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));
        operation.setDirectives(emptyList());

        return operation;
    }

    // (directives, fields)
    static Operation operationWithDirectives(List<Directive> directives, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName("");
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));
        operation.setDirectives(directives);

        return operation;
    }

    // (vars, fields)
    static Operation operation(List<Variable> vars, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName("");
        operation.setVariables(vars);
        operation.setFields(asList(fields));
        operation.setDirectives(emptyList());

        return operation;
    }

    // (vars, directives, fields)
    static Operation operationWithDirectives(List<Variable> vars, List<Directive> directives, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName("");
        operation.setVariables(vars);
        operation.setFields(asList(fields));
        operation.setDirectives(directives);

        return operation;
    }

    // (type, fields)
    static Operation operation(OperationType type, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName("");
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));
        operation.setDirectives(emptyList());

        return operation;
    }

    // (type, directives, fields)
    static Operation operationWithDirectives(OperationType type,
            List<Directive> directives,
            FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName("");
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));
        operation.setDirectives(directives);

        return operation;
    }

    // (type, vars, fields)
    static Operation operation(OperationType type, List<Variable> vars, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName("");
        operation.setVariables(vars);
        operation.setFields(asList(fields));
        operation.setDirectives(emptyList());

        return operation;
    }

    // (type, vars, directives, fields)
    static Operation operationWithDirectives(OperationType type,
            List<Variable> vars,
            List<Directive> directives,
            FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName("");
        operation.setVariables(vars);
        operation.setFields(asList(fields));
        operation.setDirectives(directives);

        return operation;
    }

    // (name, fields)
    static Operation operation(String name, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName(name);
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));
        operation.setDirectives(emptyList());

        return operation;
    }

    // (name, directives, fields)
    static Operation operationWithDirectives(String name, List<Directive> directives, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName(name);
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));
        operation.setDirectives(directives);

        return operation;
    }

    // (type, name, fields)
    static Operation operation(OperationType type, String name, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName(name);
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));
        operation.setDirectives(emptyList());

        return operation;
    }

    // (type, name, directives, fields)
    static Operation operationWithDirectives(OperationType type,
            String name,
            List<Directive> directives,
            FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName(name);
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));
        operation.setDirectives(directives);

        return operation;
    }

    // (name, vars, fields)
    static Operation operation(String name, List<Variable> vars, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName(name);
        operation.setVariables(vars);
        operation.setFields(asList(fields));
        operation.setDirectives(emptyList());

        return operation;
    }

    // (name, vars, directives, fields)
    static Operation operationWithDirectives(String name,
            List<Variable> vars,
            List<Directive> directives,
            FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName(name);
        operation.setVariables(vars);
        operation.setFields(asList(fields));
        operation.setDirectives(directives);

        return operation;
    }

    // (type, name, vars, fields)
    static Operation operation(OperationType type, String name, List<Variable> vars, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName(name);
        operation.setVariables(vars);
        operation.setFields(asList(fields));
        operation.setDirectives(emptyList());

        return operation;
    }

    // (type, name, vars, directives, fields)
    static Operation operationWithDirectives(OperationType type,
            String name,
            List<Variable> vars,
            List<Directive> directives,
            FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName(name);
        operation.setVariables(vars);
        operation.setFields(asList(fields));
        operation.setDirectives(directives);

        return operation;
    }

    /*
     * Getter/Setter
     */
    OperationType getType();

    void setType(OperationType type);

    String getName();

    void setName(String name);

    List<Variable> getVariables();

    void setVariables(List<Variable> vars);

    List<FieldOrFragment> getFields();

    void setFields(List<FieldOrFragment> fields);

    List<Directive> getDirectives();

    void setDirectives(List<Directive> directives);
}

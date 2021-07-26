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

        return operation;
    }

    // (vars, fields)
    static Operation operation(List<Variable> vars, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName("");
        operation.setVariables(vars);
        operation.setFields(asList(fields));

        return operation;
    }

    // (type, fields)
    static Operation operation(OperationType type, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName("");
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));

        return operation;
    }

    // (type, vars, fields)
    static Operation operation(OperationType type, List<Variable> vars, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName("");
        operation.setVariables(vars);
        operation.setFields(asList(fields));

        return operation;
    }

    // (name, fields)
    static Operation operation(String name, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName(name);
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));

        return operation;
    }

    // (type, name, fields)
    static Operation operation(OperationType type, String name, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName(name);
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));

        return operation;
    }

    // (name, vars, fields)
    static Operation operation(String name, List<Variable> vars, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName(name);
        operation.setVariables(vars);
        operation.setFields(asList(fields));

        return operation;
    }

    // (type, name, vars, fields)
    static Operation operation(OperationType type, String name, List<Variable> vars, FieldOrFragment... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName(name);
        operation.setVariables(vars);
        operation.setFields(asList(fields));

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
}

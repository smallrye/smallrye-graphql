package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.OperationType.QUERY;
import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

public interface Operation extends Buildable {
    /*
     * Helpers
     */
    static List<Operation> operations(Operation... operations) {
        return asList(operations);
    }

    // (fields)
    static Operation operation(Field... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName("");
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));

        return operation;
    }

    // (vars, fields)
    static Operation operation(List<Variable> vars, Field... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName("");
        operation.setVariables(vars);
        operation.setFields(asList(fields));

        return operation;
    }

    // (type, fields)
    static Operation operation(OperationType type, Field... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName("");
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));

        return operation;
    }

    // (type, vars, fields)
    static Operation operation(OperationType type, List<Variable> vars, Field... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName("");
        operation.setVariables(vars);
        operation.setFields(asList(fields));

        return operation;
    }

    // (name, fields)
    static Operation operation(String name, Field... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName(name);
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));

        return operation;
    }

    // (type, name, fields)
    static Operation operation(OperationType type, String name, Field... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(type);
        operation.setName(name);
        operation.setVariables(emptyList());
        operation.setFields(asList(fields));

        return operation;
    }

    // (name, vars, fields)
    static Operation operation(String name, List<Variable> vars, Field... fields) {
        Operation operation = getNewInstanceOf(Operation.class);

        operation.setType(QUERY);
        operation.setName(name);
        operation.setVariables(vars);
        operation.setFields(asList(fields));

        return operation;
    }

    // (type, name, vars, fields)
    static Operation operation(OperationType type, String name, List<Variable> vars, Field... fields) {
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

    List<Field> getFields();

    void setFields(List<Field> fields);
}

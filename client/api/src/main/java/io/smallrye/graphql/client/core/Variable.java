package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.VariableType.varType;
import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceOf;
import static java.util.Arrays.asList;

import java.util.List;
import java.util.Optional;

public interface Variable extends Buildable {
    /*
     * Helpers
     */
    static List<Variable> vars(Variable... vars) {
        return asList(vars);
    }

    // (name, scalarType)
    static Variable var(String name, ScalarType scalarType) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType(scalarType));
        var.setDefaultValue(Optional.empty());

        return var;
    }

    // (name, scalarType, defaultValue)
    static Variable var(String name, ScalarType scalarType, Object defaultValue) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType(scalarType));
        var.setDefaultValue(Optional.of(defaultValue));

        return var;
    }

    // (name, objectType)
    static Variable var(String name, String objectTypeName) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType(objectTypeName));
        var.setDefaultValue(Optional.empty());

        return var;
    }

    // (name, objectType, defaultValue)
    static Variable var(String name, String objectTypeName, Object defaultValue) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType(objectTypeName));
        var.setDefaultValue(Optional.of(defaultValue));

        return var;
    }

    // (name, variableType)
    static Variable var(String name, VariableType varType) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType);
        var.setDefaultValue(Optional.empty());

        return var;
    }

    // (name, variableType, defaultValue)
    static Variable var(String name, VariableType varType, Object defaultValue) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType);
        var.setDefaultValue(Optional.of(defaultValue));

        return var;
    }

    /*
     * Getter/Setter
     */
    String getName();

    void setName(String name);

    VariableType getType();

    void setType(VariableType value);

    Optional<Object> getDefaultValue();

    void setDefaultValue(Optional<Object> value);
}

package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.VariableType.varType;
import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

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
        var.setDirectives(emptyList());

        return var;
    }

    // (name, scalarType, directives)
    static Variable varWithDirectives(String name, ScalarType scalarType, List<Directive> directives) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType(scalarType));
        var.setDefaultValue(Optional.empty());
        var.setDirectives(directives);

        return var;
    }

    // (name, scalarType, defaultValue)
    static Variable var(String name, ScalarType scalarType, Object defaultValue) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType(scalarType));
        var.setDefaultValue(Optional.of(defaultValue));
        var.setDirectives(emptyList());

        return var;
    }
    // (name, scalarType, defaultValue, directives)

    static Variable varWithDirectives(String name,
            ScalarType scalarType,
            Object defaultValue,
            List<Directive> directives) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType(scalarType));
        var.setDefaultValue(Optional.of(defaultValue));
        var.setDirectives(directives);

        return var;
    }

    // (name, objectType)
    static Variable var(String name, String objectTypeName) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType(objectTypeName));
        var.setDefaultValue(Optional.empty());
        var.setDirectives(emptyList());

        return var;
    }

    // (name, objectType, directives)
    static Variable varWithDirectives(String name, String objectTypeName, List<Directive> directives) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType(objectTypeName));
        var.setDefaultValue(Optional.empty());
        var.setDirectives(directives);

        return var;
    }

    // (name, objectType, defaultValue)
    static Variable var(String name, String objectTypeName, Object defaultValue) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType(objectTypeName));
        var.setDefaultValue(Optional.of(defaultValue));
        var.setDirectives(emptyList());

        return var;
    }

    // (name, objectType, defaultValue, directives)
    static Variable varWithDirectives(String name, String objectTypeName, Object defaultValue, List<Directive> directives) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType(objectTypeName));
        var.setDefaultValue(Optional.of(defaultValue));
        var.setDirectives(directives);

        return var;
    }

    // (name, variableType)
    static Variable var(String name, VariableType varType) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType);
        var.setDefaultValue(Optional.empty());
        var.setDirectives(emptyList());

        return var;
    }

    // (name, variableType, directives)
    static Variable varWithDirectives(String name, VariableType varType, List<Directive> directives) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType);
        var.setDefaultValue(Optional.empty());
        var.setDirectives(directives);

        return var;
    }

    // (name, variableType, defaultValue)
    static Variable var(String name, VariableType varType, Object defaultValue) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType);
        var.setDefaultValue(Optional.of(defaultValue));
        var.setDirectives(emptyList());
        return var;
    }

    // (name, variableType, defaultValue, directives)
    static Variable varWithDirectives(String name, VariableType varType, Object defaultValue, List<Directive> directives) {
        Variable var = getNewInstanceOf(Variable.class);

        var.setName(name);
        var.setType(varType);
        var.setDefaultValue(Optional.of(defaultValue));
        var.setDirectives(directives);
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

    List<Directive> getDirectives();

    void setDirectives(List<Directive> directives);
}

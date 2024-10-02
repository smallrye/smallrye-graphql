package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceFromFactory;
import static io.smallrye.graphql.client.core.utils.validation.NameValidation.validateName;

import io.smallrye.graphql.client.core.factory.VariableTypeFactory;

public interface VariableType extends Buildable {

    /*
     * Static factory methods
     */
    // (objectTypeName)
    static VariableType varType(String objectTypeName) {
        VariableType varType = getNewInstanceFromFactory(VariableTypeFactory.class);

        varType.setName(validateName(objectTypeName));
        varType.setNonNull(false);
        varType.setChild(null);

        return varType;
    }

    // (scalarType)
    static VariableType varType(ScalarType scalarType) {
        VariableType varType = getNewInstanceFromFactory(VariableTypeFactory.class);

        varType.setName(scalarType.toString());
        varType.setNonNull(false);
        varType.setChild(null);

        return varType;
    }

    // nonNull(scalarType)
    static VariableType nonNull(ScalarType scalarType) {
        VariableType varType = getNewInstanceFromFactory(VariableTypeFactory.class);

        varType.setName(scalarType.toString());
        varType.setNonNull(true);
        varType.setChild(null);

        return varType;
    }

    // nonNull(objectTypeName)
    static VariableType nonNull(String objectTypeName) {
        VariableType varType = getNewInstanceFromFactory(VariableTypeFactory.class);

        varType.setName(validateName(objectTypeName));
        varType.setNonNull(true);
        varType.setChild(null);

        return varType;
    }

    // nonNull(varType)
    static VariableType nonNull(VariableType varType) {
        varType.setNonNull(true);
        return varType;
    }

    // list(scalarType)
    static VariableType list(ScalarType scalarType) {
        VariableType varType = getNewInstanceFromFactory(VariableTypeFactory.class);

        varType.setName("list(" + scalarType.toString() + ")");
        varType.setNonNull(false);
        varType.setChild(varType(scalarType));

        return varType;
    }

    // list(typeName)
    static VariableType list(String objectTypeName) {
        VariableType varType = getNewInstanceFromFactory(VariableTypeFactory.class);

        varType.setName("list(" + validateName(objectTypeName) + ")");
        varType.setNonNull(false);
        varType.setChild(varType(objectTypeName));

        return varType;
    }

    // list(variableType)
    static VariableType list(VariableType childVarType) {
        VariableType varType = getNewInstanceFromFactory(VariableTypeFactory.class);

        varType.setName("list(" + childVarType.getName() + ")");
        varType.setNonNull(false);
        varType.setChild(childVarType);

        return varType;
    }

    /*
     * Getter/Setter
     */
    String getName();

    void setName(String name);

    boolean isNonNull();

    void setNonNull(boolean nonNull);

    VariableType getChild();

    void setChild(VariableType child);

    default boolean isList() {
        return getChild() != null;
    }
}

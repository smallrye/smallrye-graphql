package io.smallrye.graphql.schema.model;

import static io.smallrye.graphql.schema.model.CustomScalarType.CustomScalarPrimitiveType.FLOAT_TYPE;
import static io.smallrye.graphql.schema.model.CustomScalarType.CustomScalarPrimitiveType.INT_TYPE;
import static io.smallrye.graphql.schema.model.CustomScalarType.CustomScalarPrimitiveType.STRING_TYPE;

import java.util.Set;

public final class CustomScalarType extends Reference {

    private String description;
    private CustomScalarPrimitiveType customScalarPrimitiveType;

    public CustomScalarType() {
    }

    public CustomScalarType(String className, String name, String description, Set<String> interfaces) {
        super(className, name, ReferenceType.SCALAR);
        this.description = description;
        if (interfaces.contains("io.smallrye.graphql.scalar.custom.CustomIntScalar")) {
            customScalarPrimitiveType = INT_TYPE;
        } else if (interfaces.contains("io.smallrye.graphql.scalar.custom.CustomFloatScalar")) {
            customScalarPrimitiveType = FLOAT_TYPE;
        } else if (interfaces.contains("io.smallrye.graphql.scalar.custom.CustomStringScalar")) {
            customScalarPrimitiveType = STRING_TYPE;
        } else {
            //TODO error handle in the expected way
            throw new RuntimeException("Required to implement a known CustomScalar primitive type. "
                    + "(CustomStringScalar, CustomFloatScalar, CustomIntScalar");
        }
    }

    public String getDescription() {
        return description;
    }

    public CustomScalarPrimitiveType customScalarPrimitiveType() {
        return customScalarPrimitiveType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CustomScalarType{" +
                "name='" + getName() + '\'' +
                ", description='" + description + '\'' +
                ", customScalar='" + customScalarPrimitiveType.name() + '\'' +
                '}';
    }

    public enum CustomScalarPrimitiveType {
        STRING_TYPE,
        INT_TYPE,
        FLOAT_TYPE
    }
}

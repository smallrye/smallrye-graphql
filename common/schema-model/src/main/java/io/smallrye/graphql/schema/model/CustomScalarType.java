package io.smallrye.graphql.schema.model;

import static io.smallrye.graphql.schema.model.CustomScalarType.CustomScalarPrimitiveType.FLOAT_TYPE;
import static io.smallrye.graphql.schema.model.CustomScalarType.CustomScalarPrimitiveType.INT_TYPE;
import static io.smallrye.graphql.schema.model.CustomScalarType.CustomScalarPrimitiveType.STRING_TYPE;

public final class CustomScalarType extends Reference {

    private String description;
    private CustomScalarPrimitiveType customScalarPrimitiveType;

    public CustomScalarType() {
    }

    public CustomScalarType(String className, String name, String description,
            CustomScalarPrimitiveType customScalarPrimitiveType) {
        super(className, name, ReferenceType.SCALAR);
        this.description = description;
        this.customScalarPrimitiveType = customScalarPrimitiveType;
    }

    public String getDescription() {
        return description;
    }

    public CustomScalarPrimitiveType getCustomScalarPrimitiveType() {
        return customScalarPrimitiveType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCustomScalarPrimitiveType(CustomScalarPrimitiveType customScalarPrimitiveType) {
        this.customScalarPrimitiveType = customScalarPrimitiveType;
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

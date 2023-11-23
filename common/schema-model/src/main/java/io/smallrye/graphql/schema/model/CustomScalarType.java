package io.smallrye.graphql.schema.model;

public final class CustomScalarType extends Reference {

    private String description;

    public CustomScalarType() {
    }

    public CustomScalarType(String className, String name, String description) {
        super(className, name, ReferenceType.SCALAR);
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CustomScalarType{" +
                ", name='" + getName() + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}

package io.smallrye.graphql.schema.model;

/**
 * Represents a return (output) to a method
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Return extends Field {
    private Reference returnType;

    public Return(String name, String description, Reference returnType) {
        super.setName(name);
        super.setDescription(description);
        this.returnType = returnType;
    }

    public Reference getReturnType() {
        return returnType;
    }

}

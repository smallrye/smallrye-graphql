package io.smallrye.graphql.schema.model;

/**
 * Represents a return (output) to a method
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class Return extends Field {
    private Reference returnType;

    public Reference getReturnType() {
        return returnType;
    }

    public void setReturnType(Reference returnType) {
        this.returnType = returnType;
    }
}

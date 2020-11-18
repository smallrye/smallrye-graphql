package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Indicate that a field should apply map type
 * 
 * At the moment this is used to map existing scalars to other scalars
 * or custom objects to existing scalars.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Mapping implements Serializable {

    private Reference reference;
    private Create create = Create.NONE;

    public Mapping() {
    }

    public Mapping(Reference reference) {
        this.reference = reference;
    }

    public Reference getReference() {
        return reference;
    }

    public void setReference(Reference reference) {
        this.reference = reference;
    }

    public Create getCreate() {
        return create;
    }

    public void setCreate(Create create) {
        this.create = create;
    }

    /**
     * Indicate how an instance of the mapped object can be created
     */
    public enum Create {
        CONSTRUCTOR,
        SET_VALUE,
        STATIC_FROM,
        NONE
    }
}

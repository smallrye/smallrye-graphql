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
    private String deserializeMethod = null; // default null (pass through)
    private String serializeMethod = "toString"; // default try and use toString

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

    public String getDeserializeMethod() {
        return deserializeMethod;
    }

    public void setDeserializeMethod(String deserializeMethod) {
        this.deserializeMethod = deserializeMethod;
    }

    public String getSerializeMethod() {
        return serializeMethod;
    }

    public void setSerializeMethod(String serializeMethod) {
        this.serializeMethod = serializeMethod;
    }
}

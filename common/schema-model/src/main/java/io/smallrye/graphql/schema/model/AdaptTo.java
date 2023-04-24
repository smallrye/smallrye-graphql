package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Indicate that a field should adapt to another type
 *
 * At the moment this is used to adapt existing scalars to other scalars
 * or custom objects to existing scalars.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AdaptTo implements Serializable {

    private Reference reference;
    private String deserializeMethod = null; // default null (pass through)
    private String serializeMethod = "toString"; // default try and use toString

    public AdaptTo() {
    }

    public AdaptTo(Reference reference) {
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

    @Override
    public String toString() {
        return "AdaptTo{" + "reference=" + reference + ", deserializeMethod=" + deserializeMethod + ", serializeMethod="
                + serializeMethod + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.reference);
        hash = 23 * hash + Objects.hashCode(this.deserializeMethod);
        hash = 23 * hash + Objects.hashCode(this.serializeMethod);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AdaptTo other = (AdaptTo) obj;
        if (!Objects.equals(this.deserializeMethod, other.deserializeMethod)) {
            return false;
        }
        if (!Objects.equals(this.serializeMethod, other.serializeMethod)) {
            return false;
        }
        if (!Objects.equals(this.reference, other.reference)) {
            return false;
        }
        return true;
    }

}

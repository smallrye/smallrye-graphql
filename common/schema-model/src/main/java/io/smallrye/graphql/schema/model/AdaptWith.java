package io.smallrye.graphql.schema.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Indicate that a field apply a custom adapter, adapting from one type to another
 *
 * This can be a JsonbTypeAdapter
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class AdaptWith implements Serializable {

    private String adapterInterface; // The interface implemented by the user
    private String fromMethod; // The from method defined by that interface
    private String toMethod; // The to method defined by that interface

    private String adapterClass; // The concrete class implementing the adapterInterface
    private Reference fromReference; // The class we are adapting from
    private Reference toReference; // The class we are adapting to

    public AdaptWith() {
    }

    public AdaptWith(String adapterInterface, String fromMethod, String toMethod) {
        this(adapterInterface, fromMethod, toMethod, null, null, null);
    }

    public AdaptWith(String adapterInterface, String fromMethod, String toMethod, String adapterClass, Reference fromReference,
            Reference toReference) {
        this.adapterInterface = adapterInterface;
        this.fromMethod = fromMethod;
        this.toMethod = toMethod;
        this.adapterClass = adapterClass;
        this.fromReference = fromReference;
        this.toReference = toReference;
    }

    public String getAdapterInterface() {
        return adapterInterface;
    }

    public void setAdapterInterface(String adapterInterface) {
        this.adapterInterface = adapterInterface;
    }

    public String getFromMethod() {
        return fromMethod;
    }

    public void setFromMethod(String fromMethod) {
        this.fromMethod = fromMethod;
    }

    public String getToMethod() {
        return toMethod;
    }

    public void setToMethod(String toMethod) {
        this.toMethod = toMethod;
    }

    public String getAdapterClass() {
        return adapterClass;
    }

    public void setAdapterClass(String adapterClass) {
        this.adapterClass = adapterClass;
    }

    public Reference getFromReference() {
        return fromReference;
    }

    public void setFromReference(Reference fromReference) {
        this.fromReference = fromReference;
    }

    public Reference getToReference() {
        return toReference;
    }

    public void setToReference(Reference toReference) {
        this.toReference = toReference;
    }

    public boolean isJsonB() {
        return this.adapterInterface.equals("javax.json.bind.adapter.JsonbAdapter")
                || this.adapterInterface.equals("jakarta.json.bind.adapter.JsonbAdapter");
    }

    @Override
    public String toString() {
        return "AdaptWith{" + "adapterInterface=" + adapterInterface + ", fromMethod=" + fromMethod + ", toMethod=" + toMethod
                + ", adapterClass=" + adapterClass + ", fromReference=" + fromReference + ", toReference=" + toReference + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.adapterInterface);
        hash = 97 * hash + Objects.hashCode(this.fromMethod);
        hash = 97 * hash + Objects.hashCode(this.toMethod);
        hash = 97 * hash + Objects.hashCode(this.adapterClass);
        hash = 97 * hash + Objects.hashCode(this.fromReference);
        hash = 97 * hash + Objects.hashCode(this.toReference);
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
        final AdaptWith other = (AdaptWith) obj;
        if (!Objects.equals(this.adapterInterface, other.adapterInterface)) {
            return false;
        }
        if (!Objects.equals(this.fromMethod, other.fromMethod)) {
            return false;
        }
        if (!Objects.equals(this.toMethod, other.toMethod)) {
            return false;
        }
        if (!Objects.equals(this.adapterClass, other.adapterClass)) {
            return false;
        }
        if (!Objects.equals(this.fromReference, other.fromReference)) {
            return false;
        }
        if (!Objects.equals(this.toReference, other.toReference)) {
            return false;
        }
        return true;
    }

}

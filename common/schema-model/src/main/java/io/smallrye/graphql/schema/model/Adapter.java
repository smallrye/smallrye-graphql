package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Indicate that a field apply an adapter
 * 
 * This can be a JsonbTypeAdapter
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Adapter implements Serializable {

    private String adapterInterface; // The interface implemented by the user
    private String fromMethod; // The from method defined by that interface
    private String toMethod; // The to method defined by that interface

    private String adapterClass; // The concrete class implementing the adapterInterface
    private String fromClass; // The class we are adapting from
    private String toClass; // The class we are adapting to

    public Adapter() {
    }

    public Adapter(String adapterInterface, String fromMethod, String toMethod) {
        this(adapterInterface, fromMethod, toMethod, null, null, null);
    }

    public Adapter(String adapterInterface, String fromMethod, String toMethod, String adapterClass, String fromClass,
            String toClass) {
        this.adapterInterface = adapterInterface;
        this.fromMethod = fromMethod;
        this.toMethod = toMethod;
        this.adapterClass = adapterClass;
        this.fromClass = fromClass;
        this.toClass = toClass;
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

    public String getFromClass() {
        return fromClass;
    }

    public void setFromClass(String fromClass) {
        this.fromClass = fromClass;
    }

    public String getToClass() {
        return toClass;
    }

    public void setToClass(String toClass) {
        this.toClass = toClass;
    }

    public boolean isJsonB() {
        return this.adapterInterface.equals("javax.json.bind.adapter.JsonbAdapter")
                || this.adapterInterface.equals("jakarta.json.bind.adapter.JsonbAdapter");
    }
}

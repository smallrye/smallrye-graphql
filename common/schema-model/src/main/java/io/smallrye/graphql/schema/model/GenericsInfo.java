package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * If the type is wraped in a generics bucket, keep the info here.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GenericsInfo implements Serializable {

    private String holderClassName;

    public GenericsInfo() {
    }

    public GenericsInfo(String holderClassName) {
        this.holderClassName = holderClassName;
    }

    public String getHolderClassName() {
        return holderClassName;
    }

    public void setHolderClassName(String holderClassName) {
        this.holderClassName = holderClassName;
    }
}

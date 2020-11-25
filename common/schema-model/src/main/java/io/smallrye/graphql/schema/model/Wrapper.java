package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * If the type is wrapped in a generics bucket or in an array, keep the info here.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Wrapper implements Serializable {

    private String wrapperClassName;
    private boolean notEmpty = false; // Mark this to be not empty
    private WrapperType wrapperType = WrapperType.UNKNOWN;

    private Wrapper wrapper = null;

    public Wrapper() {
    }

    public Wrapper(WrapperType wrapperType, String wrapperClassName) {
        this.wrapperType = wrapperType;
        this.wrapperClassName = wrapperClassName;
    }

    public Wrapper(WrapperType wrapperType, String wrapperClassName, boolean notEmpty) {
        this.wrapperType = wrapperType;
        this.wrapperClassName = wrapperClassName;
        this.notEmpty = notEmpty;
    }

    public WrapperType getWrapperType() {
        return wrapperType;
    }

    public void setWrapperType(WrapperType wrapperType) {
        this.wrapperType = wrapperType;
    }

    public String getWrapperClassName() {
        return wrapperClassName;
    }

    public void setWrapperClassName(String wrapperClassName) {
        this.wrapperClassName = wrapperClassName;
    }

    public void setNotEmpty(boolean notEmpty) {
        this.notEmpty = notEmpty;
    }

    public boolean isNotEmpty() {
        return this.notEmpty;
    }

    public Wrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(Wrapper wrapper) {
        this.wrapper = wrapper;
    }

    public boolean hasWrapper() {
        return this.wrapper != null;
    }

    public boolean isCollectionOrArray() {
        return isCollection() || isArray();
    }

    public boolean isCollection() {
        return wrapperType.equals(WrapperType.COLLECTION);
    }

    public boolean isArray() {
        return wrapperType.equals(WrapperType.ARRAY);
    }

    public boolean isOptional() {
        return wrapperType.equals(WrapperType.OPTIONAL);
    }

    public boolean isUnknown() {
        return wrapperType.equals(WrapperType.UNKNOWN);
    }

    @Override
    public String toString() {
        return "Wrapper{" + "wrapperClassName=" + wrapperClassName + ", notEmpty=" + notEmpty
                + ", wrapperType=" + wrapperType + ", wrapper=" + wrapper + '}';
    }
}

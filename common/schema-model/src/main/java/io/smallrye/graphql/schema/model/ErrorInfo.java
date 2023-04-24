package io.smallrye.graphql.schema.model;

import java.io.Serializable;

/**
 * Represent info on a Exception.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public final class ErrorInfo implements Serializable {

    private String className;
    private String errorCode;

    public ErrorInfo() {
    }

    public ErrorInfo(String className, String errorCode) {
        this.className = className;
        this.errorCode = errorCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "ErrorInfo{" + "className=" + className + ", errorCode=" + errorCode + '}';
    }
}
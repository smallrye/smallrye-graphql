package io.smallrye.graphql.execution.event;

import java.lang.reflect.Method;

/**
 * Hold some information about the invocation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class InvokeInfo {

    private Object operationInstance;
    private Method operationMethod;
    private Object[] operationTransformedArguments;

    public InvokeInfo() {
    }

    public InvokeInfo(Object operationInstance, Method operationMethod, Object[] operationTransformedArguments) {
        this.operationInstance = operationInstance;
        this.operationMethod = operationMethod;
        this.operationTransformedArguments = operationTransformedArguments;
    }

    public Object getOperationInstance() {
        return operationInstance;
    }

    public void setOperationInstance(Object operationInstance) {
        this.operationInstance = operationInstance;
    }

    public Method getOperationMethod() {
        return operationMethod;
    }

    public void setOperationMethod(Method operationMethod) {
        this.operationMethod = operationMethod;
    }

    public Object[] getOperationTransformedArguments() {
        return operationTransformedArguments;
    }

    public void setOperationTransformedArguments(Object[] operationTransformedArguments) {
        this.operationTransformedArguments = operationTransformedArguments;
    }
}

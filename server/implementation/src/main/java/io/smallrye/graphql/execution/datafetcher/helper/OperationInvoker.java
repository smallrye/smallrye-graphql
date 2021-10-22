package io.smallrye.graphql.execution.datafetcher.helper;

import java.util.LinkedList;
import java.util.List;

import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Invoke methods on Operation class
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class OperationInvoker extends ReflectionInvoker {

    public OperationInvoker(Operation operation) {
        super(operation.getClassName());
        super.setMethod(operation.getMethodName(), getParameterClasses(operation));
    }

    private List<String> getParameterClasses(Operation operation) {
        if (operation.hasArguments()) {
            List<String> cl = new LinkedList<>();
            for (Field argument : operation.getArguments()) {
                if (argument.hasWrapper()) {
                    cl.add(argument.getWrapper().getWrapperClassName());
                } else {
                    cl.add(argument.getReference().getClassName());
                }
            }
            return cl;
        }
        return null;
    }
}

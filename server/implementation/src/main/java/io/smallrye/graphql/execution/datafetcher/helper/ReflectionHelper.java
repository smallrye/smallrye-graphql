package io.smallrye.graphql.execution.datafetcher.helper;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLException;

import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.execution.event.InvokeInfo;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.spi.ClassloadingService;
import io.smallrye.graphql.spi.LookupService;

/**
 * Help with reflection on an operation
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReflectionHelper {

    private final LookupService lookupService = LookupService.load();
    private final ClassloadingService classloadingService = ClassloadingService.load();

    private final Operation operation;
    private Object operationInstance;
    private Method method;

    public ReflectionHelper(Operation operation) {
        this.operation = operation;
    }

    public <T> T invoke(Object... arguments) throws Exception {

        init();

        try {
            EventEmitter.fireBeforeMethodInvoke(new InvokeInfo(operationInstance, method, arguments));
            return (T) this.method.invoke(this.operationInstance, arguments);
        } catch (InvocationTargetException ex) {
            //Invoked method has thrown something, unwrap
            Throwable throwable = ex.getCause();

            if (throwable instanceof Error) {
                throw (Error) throwable;
            } else if (throwable instanceof GraphQLException) {
                throw (GraphQLException) throwable;
            } else if (throwable instanceof Exception) {
                throw (Exception) throwable;
            } else {
                throw msg.dataFetcherException(operation, throwable);
            }
        }
    }

    private void init() {
        if (this.method == null) {
            Class<?> operationClass = classloadingService.loadClass(operation.getClassName());
            this.method = lookupMethod(operationClass, operation);
            this.operationInstance = lookupService.getInstance(operationClass);
        }
    }

    private Method lookupMethod(Class<?> operationClass, Operation operation) {
        try {
            return operationClass.getMethod(operation.getMethodName(), getParameterClasses(operation));
        } catch (NoSuchMethodException e) {
            throw msg.dataFetcherException(operation, e);
        }
    }

    private Class<?>[] getParameterClasses(Operation operation) {
        if (operation.hasArguments()) {
            List<Class<?>> cl = new LinkedList<>();
            for (Field argument : operation.getArguments()) {
                // If the argument is an array / collection, load that class
                if (argument.hasArray()) {
                    Class<?> clazz = classloadingService.loadClass(argument.getArray().getClassName());
                    cl.add(clazz);
                } else {
                    Class<?> clazz = classloadingService.loadClass(argument.getReference().getClassName());
                    cl.add(clazz);
                }
            }
            return cl.toArray(new Class[] {});
        }
        return null;
    }

}

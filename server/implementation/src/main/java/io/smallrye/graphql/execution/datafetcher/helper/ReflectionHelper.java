package io.smallrye.graphql.execution.datafetcher.helper;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
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

    private final LookupService lookupService = LookupService.get();
    private final ClassloadingService classloadingService = ClassloadingService.get();

    private final Operation operation;
    private final EventEmitter eventEmitter;
    private final Class<?> operationClass;
    private final Method method;

    public ReflectionHelper(Operation operation, EventEmitter eventEmitter) {
        this.operation = operation;
        this.eventEmitter = eventEmitter;
        this.operationClass = classloadingService.loadClass(operation.getClassName());
        this.method = lookupMethod(operationClass, operation);
    }

    public <T> T invokePrivileged(Object... arguments) {
        final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        return invokePrivileged(tccl, arguments);
    }

    public <T> T invokePrivileged(final ClassLoader classLoader, Object... arguments) {

        try {
            return (T) AccessController
                    .doPrivileged(new PrivilegedExceptionAction<Object>() {
                        @Override
                        public Object run() throws Exception {
                            ClassLoader originalTccl = Thread.currentThread()
                                    .getContextClassLoader();
                            Thread.currentThread().setContextClassLoader(classLoader);

                            try {
                                return invoke(arguments);
                            } finally {
                                Thread.currentThread().setContextClassLoader(classLoader);
                            }
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public <T> T invoke(Object... arguments) throws Exception {
        try {
            Object operationInstance = lookupService.getInstance(operationClass);
            eventEmitter.fireBeforeMethodInvoke(new InvokeInfo(operationInstance, method, arguments));
            return (T) this.method.invoke(operationInstance, arguments);
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

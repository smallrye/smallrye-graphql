package io.smallrye.graphql.execution.datafetcher.helper;

import static io.smallrye.graphql.SmallRyeGraphQLServerMessages.msg;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLException;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.execution.event.InvokeInfo;
import io.smallrye.graphql.spi.ClassloadingService;
import io.smallrye.graphql.spi.LookupService;

/**
 * Invoke methods using reflection
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReflectionInvoker {

    private final LookupService lookupService = LookupService.get();
    private final ClassloadingService classloadingService = ClassloadingService.get();

    private final EventEmitter eventEmitter = EventEmitter.getInstance();
    private final Class<?> operationClass;
    private Method method;
    private int injectContextAt = -1;

    public ReflectionInvoker(String className) {
        this.operationClass = classloadingService.loadClass(className);
    }

    public ReflectionInvoker(String className, String methodName, List<String> parameterClasses) {
        this.operationClass = classloadingService.loadClass(className);
        this.setMethod(methodName, parameterClasses);
    }

    public void setMethod(String methodName, List<String> parameterClasses) {
        this.method = lookupMethod(operationClass, methodName, parameterClasses);
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
                                Thread.currentThread().setContextClassLoader(originalTccl);
                            }
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    public <T> T invoke(Object... arguments) throws Exception {
        if (this.injectContextAt > -1) {
            arguments = injectContext(arguments);
        }
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
                throw msg.generalDataFetcherException(operationClass.getName() + ": " + method.getName(), throwable);
            }
        }
    }

    private Method lookupMethod(Class<?> operationClass, String methodName, List<String> parameterClasses) {
        try {
            return operationClass.getMethod(methodName, getParameterClasses(parameterClasses));
        } catch (NoSuchMethodException e) {
            throw msg.generalDataFetcherException(operationClass.getName() + ": " + methodName, e);
        }
    }

    private Class<?>[] getParameterClasses(List<String> parameterClasses) {
        if (parameterClasses != null && !parameterClasses.isEmpty()) {
            List<Class<?>> cl = new LinkedList<>();
            int cnt = 0;
            for (String className : parameterClasses) {
                cl.add(classloadingService.loadClass(className));
                if (className.equals(Context.class.getName())) {
                    this.injectContextAt = cnt;
                }
                cnt++;
            }

            return cl.toArray(new Class[] {});
        }
        return null;
    }

    private Object[] injectContext(Object[] arguments) {
        ArrayList list = new ArrayList(Arrays.asList(arguments));
        list.set(injectContextAt, SmallRyeContext.getContext());
        return list.toArray();
    }
}

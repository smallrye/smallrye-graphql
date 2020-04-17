package io.smallrye.graphql.cdi;

import java.util.ServiceLoader;

import javax.enterprise.inject.spi.CDI;

import io.smallrye.graphql.execution.Classes;

public interface CDIDelegate {

    public static CDIDelegate delegate() {
        try {
            ServiceLoader<CDIDelegate> sl = ServiceLoader.load(CDIDelegate.class);
            return sl.iterator().next();
        } catch (Exception ex) {
            return new CDIDelegate() {

                @Override
                public Class<?> getClassFromCDI(Class<?> declaringClass) {
                    Object declaringObject = getInstanceFromCDI(declaringClass);
                    return declaringObject.getClass();
                }

                @Override
                public Object getInstanceFromCDI(Class<?> declaringClass) {
                    return CDI.current().select(declaringClass).get();
                }

                @Override
                public Object getInstanceFromCDI(String declaringClass) {
                    Class<?> loadedClass = Classes.loadClass(declaringClass);
                    return getInstanceFromCDI(loadedClass);
                }
            };
        }
    }

    Class<?> getClassFromCDI(Class<?> declaringClass);

    Object getInstanceFromCDI(Class<?> declaringClass);

    Object getInstanceFromCDI(String declaringClass);

}

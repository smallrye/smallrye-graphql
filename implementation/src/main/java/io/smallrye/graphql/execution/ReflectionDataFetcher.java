/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.graphql.execution;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.spi.CDI;

import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

/**
 * Fetch data using Reflection
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReflectionDataFetcher implements DataFetcher {

    private final Method method;
    private final Class declaringClass;
    private final Class returnType;
    private final Class[] parameterClasses;
    private final boolean hasArguments;

    public ReflectionDataFetcher(MethodInfo methodInfo) {
        try {
            this.declaringClass = loadClass(methodInfo.declaringClass().name().toString());
            this.returnType = getReturnType(methodInfo);
            this.parameterClasses = getParameterClasses(methodInfo);
            this.hasArguments = parameterClasses.length != 0;

            if (hasArguments) {
                this.method = this.declaringClass.getMethod(methodInfo.name(), parameterClasses);
            } else {
                this.method = this.declaringClass.getMethod(methodInfo.name());
            }
            this.method.setAccessible(true);

        } catch (NoSuchMethodException | SecurityException | ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }

    }

    @Override
    public Object get(DataFetchingEnvironment dfe) throws Exception {
        Object declaringObject = CDI.current().select(declaringClass).get();
        Map<String, Object> arguments = dfe.getArguments();

        if (hasArguments) {
            return returnType.cast(method.invoke(declaringObject, arguments.values().toArray()));
        } else {
            return returnType.cast(method.invoke(declaringObject));
        }
    }

    private Class loadClass(String className) {
        ClassLoader classLoader = ReflectionDataFetcher.class.getClassLoader();
        Class clazz = null;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("COULD NOT FIND " + className, ex);
        }
        return clazz;
    }

    private Class getReturnType(MethodInfo methodInfo) {
        Type type = methodInfo.returnType();
        Type.Kind kind = type.kind();
        String typename = type.name().toString();
        if (kind.equals(Type.Kind.PRIMITIVE)) {
            return getPrimativeClassType(typename);
        } else {
            return loadClass(typename);
        }
    }

    private Class[] getParameterClasses(MethodInfo methodInfo) throws ClassNotFoundException {
        List<Type> parameters = methodInfo.parameters();
        Class[] inputParameters = new Class[parameters.size()];
        for (int cnt = 0; cnt < parameters.size(); cnt++) {
            Type type = parameters.get(cnt);
            Type.Kind kind = type.kind();
            String typename = type.name().toString();
            if (kind.equals(Type.Kind.PRIMITIVE)) {
                inputParameters[cnt] = getPrimativeClassType(typename);
            } else {
                inputParameters[cnt] = Class.forName(typename);
            }
        }
        return inputParameters;
    }

    private Class getPrimativeClassType(String primativeName) {
        if (primativeName.equals("boolean")) {
            return boolean.class;
        } else if (primativeName.equals("byte")) {
            return byte.class;
        } else if (primativeName.equals("char")) {
            return char.class;
        } else if (primativeName.equals("short")) {
            return short.class;
        } else if (primativeName.equals("int")) {
            return int.class;
        } else if (primativeName.equals("long")) {
            return long.class;
        } else if (primativeName.equals("float")) {
            return float.class;
        } else if (primativeName.equals("double")) {
            return double.class;
        } else {
            throw new RuntimeException("Unknown primative type [" + primativeName + "]");
        }

    }

}

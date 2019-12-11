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

package io.smallrye.graphql.execution.datafetchers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.json.bind.Jsonb;

import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.index.Classes;
import io.smallrye.graphql.schema.holder.ArgumentHolder;

/**
 * Fetch data using Reflection
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReflectionDataFetcher implements DataFetcher {
    private static final Logger LOG = Logger.getLogger(ReflectionDataFetcher.class.getName());

    private final Method method;
    private final Class declaringClass;
    private final Class returnType;
    private List<ArgumentHolder> arguments;
    private final boolean hasArguments;

    private final Map<DotName, Jsonb> inputJsonbMap;

    public ReflectionDataFetcher(MethodInfo methodInfo, List<ArgumentHolder> arguments, Map<DotName, Jsonb> inputJsonbMap) {
        try {
            this.arguments = arguments;
            this.inputJsonbMap = inputJsonbMap;
            this.declaringClass = loadClass(methodInfo.declaringClass().name().toString());
            this.returnType = getReturnType(methodInfo);
            Class[] parameterClasses = getParameterClasses(arguments);
            this.hasArguments = parameterClasses.length != 0;

            if (hasArguments) {
                this.method = this.declaringClass.getMethod(methodInfo.name(), parameterClasses);
            } else {
                this.method = this.declaringClass.getMethod(methodInfo.name());
            }
            this.method.setAccessible(true);

        } catch (NoSuchMethodException | SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object get(DataFetchingEnvironment dfe) throws Exception {
        Object declaringObject = CDI.current().select(declaringClass).get();
        return returnType.cast(method.invoke(declaringObject, getArguments(dfe).toArray()));
    }

    private ArrayList getArguments(DataFetchingEnvironment dfe) {
        ArrayList argumentObjects = new ArrayList();
        for (ArgumentHolder argumentHolder : arguments) {

            String name = argumentHolder.getName();
            Class type = argumentHolder.getArgumentClass();
            Object argument = getArgument(dfe, name);
            if (argument != null) {
                Type.Kind kind = argumentHolder.getType().kind();
                if (kind.equals(Type.Kind.PRIMITIVE)) {
                    // First make sure we have a primative type
                    Class givenClass = argument.getClass();
                    if (!givenClass.isPrimitive()) {
                        givenClass = Classes.toPrimativeClassType(givenClass);
                    }

                    if (givenClass.equals(type)) {
                        argumentObjects.add(argument);
                    } else if (givenClass.equals(String.class)) {
                        // We go a String, but not expecting one. Lets create new primative
                        argumentObjects.add(Classes.stringToPrimative(argument.toString(), type));
                    }
                } else if (kind.equals(Type.Kind.PARAMETERIZED_TYPE)) {
                    argumentObjects.add(argument); // TODO: Test propper Map<Pojo> and List<Pojo>
                } else if (kind.equals(Type.Kind.CLASS)) {
                    Class givenClass = argument.getClass();
                    if (givenClass.equals(type)) {
                        argumentObjects.add(argument);
                    } else if (Map.class.isAssignableFrom(argument.getClass())) {
                        argumentObjects.add(toPojo(Map.class.cast(argument), type));
                    } else if (givenClass.equals(String.class)) {
                        // We go a String, but not expecting one. Lets bind to Pojo with JsonB
                        argumentObjects.add(toPojo(argument.toString(), type));
                    }

                } else {
                    LOG.warn("Not sure what to do with [" + kind.name() + "] kind");
                    argumentObjects.add(argument);
                }
            }
        }
        return argumentObjects;
    }

    private Object getArgument(DataFetchingEnvironment dfe, String name) {
        Object argument = dfe.getArgument(name);
        if (argument != null) {
            return argument;
        }
        Object source = dfe.getSource();
        if (source != null) {
            return source;
        }
        return null;
    }

    private Object toPojo(String json, Class type) {
        Jsonb jsonb = getJsonbForType(type);
        return jsonb.fromJson(json, type);
    }

    private Object toPojo(Map m, Class type) {
        Jsonb jsonb = getJsonbForType(type);
        String json = jsonb.toJson(m);
        Object o = jsonb.fromJson(json, type);
        return o;
    }

    private Jsonb getJsonbForType(Class type) {
        DotName key = DotName.createSimple(type.getName());
        return inputJsonbMap.get(key);
    }

    private Class loadClass(String className) {
        ClassLoader classLoader = ReflectionDataFetcher.class.getClassLoader();
        Class clazz = null;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Could not find class [" + className + "]", ex);
        }
        return clazz;
    }

    private Class getReturnType(MethodInfo methodInfo) {
        Type type = methodInfo.returnType();
        Type.Kind kind = type.kind();
        String typename = type.name().toString();
        if (kind.equals(Type.Kind.PRIMITIVE)) {
            return Classes.getPrimativeClassType(typename);
        } else {
            return loadClass(typename);
        }
    }

    private Class[] getParameterClasses(List<ArgumentHolder> arguments) {
        List<Class> cl = new ArrayList<>();
        for (ArgumentHolder argumentHolder : arguments) {
            cl.add(argumentHolder.getArgumentClass());
        }
        return cl.toArray(new Class[] {});
    }
}

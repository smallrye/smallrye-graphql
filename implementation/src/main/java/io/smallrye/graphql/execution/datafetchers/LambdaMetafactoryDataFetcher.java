/*
 * Copyright 2020 Red Hat, Inc.
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

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Function;

import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

/**
 * Fetch data using LambdaMetafactory
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 * 
 *         TODO: Implement this after speed test is in place
 */
public class LambdaMetafactoryDataFetcher implements DataFetcher {
    private static final Logger LOG = Logger.getLogger(LambdaMetafactoryDataFetcher.class.getName());
    private MethodInfo methodInfo;

    public LambdaMetafactoryDataFetcher(MethodInfo methodInfo) {

        this.methodInfo = methodInfo;
        //String service = CDI.current().select(Class.forName(declaringClass.simpleName())).get();

        //} catch (ClassNotFoundException ex) {
        //    throw new RuntimeException(ex);
        //}

    }

    @Override
    public Object get(DataFetchingEnvironment dfe) throws Exception {

        Class declaringClass = loadClass(this.methodInfo.declaringClass().name().toString());
        Class returnType = loadClass(this.methodInfo.returnType().name().toString());
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        CallSite site = LambdaMetafactory.metafactory(lookup,
                "apply",
                MethodType.methodType(Function.class),
                MethodType.methodType(Object.class, Object.class),
                lookup.findVirtual(declaringClass, methodInfo.name(), MethodType.methodType(returnType)),
                MethodType.methodType(returnType, declaringClass));

        try {
            Function function = (Function) site.getTarget().invokeWithArguments(methodInfo.parameters());

        } catch (Throwable ex) {
            LOG.error(ex);
        }

        return null;
    }

    private Class loadClass(String className) {
        ClassLoader classLoader = LambdaMetafactoryDataFetcher.class.getClassLoader();
        Class clazz = null;
        try {
            clazz = classLoader.loadClass(className);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("COULD NOT FIND " + className, ex);
        }
        return clazz;
    }

}

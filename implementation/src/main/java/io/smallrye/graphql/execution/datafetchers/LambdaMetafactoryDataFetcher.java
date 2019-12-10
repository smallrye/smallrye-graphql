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

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
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
        printInfo(dfe);
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
            //LOG.error("=============================================================");
            //LOG.error("====> " + function.apply(declaringClass.cast(dfe.getSource())));
            //LOG.error("=============================================================");
        } catch (Throwable ex) {
            LOG.error("Poo poo", ex);
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

    private void printInfo(DataFetchingEnvironment dfe) {
        LOG.error("========================= [" + this.methodInfo.name() + "] ====================================");
        LOG.error("====> CONTEXT: " + dfe.getContext().getClass().getName());
        LOG.error("====> LOCALCONTEXT: " + dfe.getLocalContext().getClass().getName());
        LOG.error("====> ROOT: " + dfe.getRoot().getClass().getName());
        LOG.error("====> SOURCE: " + dfe.getSource().getClass().getName());
        LOG.error("====> DOCUMENT: " + dfe.getDocument().toString());
        LOG.error("====> EXECUTION ID: " + dfe.getExecutionId());
        LOG.error("====> EXECUTION STEP INFO: " + dfe.getExecutionStepInfo().simplePrint());
        LOG.error("====> FIELD: " + dfe.getField());
        LOG.error("====> FIELD DEFINITION: " + dfe.getFieldDefinition());
        LOG.error("====> FIELD TYPE: " + dfe.getFieldType());
        LOG.error("====> FRAGMENTS: " + dfe.getFragmentsByName());
        LOG.error("====> OPERATION DEFINITION: " + dfe.getOperationDefinition());
        LOG.error("====> PARENT TYPE: " + dfe.getParentType().getName());
        LOG.error("====> MERGED FIELD: " + dfe.getMergedField());
        LOG.error("====> QUERY DIRECTIVES: " + dfe.getQueryDirectives());
        LOG.error("====> SELECTION SET: " + dfe.getSelectionSet());
        LOG.error("====> VARIABLES: " + dfe.getVariables());
        LOG.error("==== ARGUMENTS ====");
        Map<String, Object> arguments = dfe.getArguments();
        for (Map.Entry<String, Object> argument : arguments.entrySet()) {
            LOG.error("\t" + argument.getKey() + " = " + argument.getValue());
        }

        LOG.error("=============================================================");
    }

}

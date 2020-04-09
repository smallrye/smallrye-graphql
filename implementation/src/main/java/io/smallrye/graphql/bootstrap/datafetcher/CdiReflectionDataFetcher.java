package io.smallrye.graphql.bootstrap.datafetcher;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLException;
import org.jboss.logging.Logger;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.bootstrap.Classes;
import io.smallrye.graphql.bootstrap.TransformException;
import io.smallrye.graphql.cdi.CDIDelegate;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Method;

/**
 * Fetch data using CDI and Reflection
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CdiReflectionDataFetcher implements DataFetcher {
    private static final Logger LOG = Logger.getLogger(CdiReflectionDataFetcher.class.getName());

    private final CDIDelegate cdiDelegate = CDIDelegate.delegate();

    private final String declaringClass;
    private final Method method;
    private final Class[] parameterClasses;

    public CdiReflectionDataFetcher(String declaringClass, Method method) {
        this.declaringClass = declaringClass;
        this.method = method;
        this.parameterClasses = getParameterClasses();
    }

    @Override
    public Object get(DataFetchingEnvironment dfe) throws Exception {
        try {
            Object declaringObject = cdiDelegate.getInstanceFromCDI(declaringClass);
            Class cdiClass = declaringObject.getClass();

            if (method.hasParameters()) {
                java.lang.reflect.Method m = cdiClass.getMethod(method.getJavaName(), parameterClasses);
                //return transformableDataFetcherHelper.transform(method.invoke(declaringObject, getArguments(dfe).toArray()));
                return m.invoke(declaringObject, getArguments(dfe).toArray());
            } else {
                java.lang.reflect.Method m = cdiClass.getMethod(method.getJavaName());
                //return transformableDataFetcherHelper.transform();
                return m.invoke(declaringObject);
            }
        } catch (TransformException pe) {
            return pe.getDataFetcherResult(dfe);

        } catch (InvocationTargetException | NoSuchMethodException | SecurityException | GraphQLException
                | IllegalAccessException | IllegalArgumentException ex) {
            Throwable throwable = ex.getCause();

            if (throwable == null) {
                throw new ReflectionDataFetcherException(ex);
            } else {
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                } else if (throwable instanceof GraphQLException) {
                    GraphQLException graphQLException = (GraphQLException) throwable;
                    // return getPartialResult(dfe, graphQLException);
                    throw (Exception) throwable;
                } else {
                    throw (Exception) throwable;
                }
            }
        }
    }

    private ArrayList getArguments(DataFetchingEnvironment dfe) throws GraphQLException {
        ArrayList argumentObjects = new ArrayList();
        for (Field f : method.getParameters()) {
            Object argument = getArgument(dfe, f);
            argumentObjects.add(argument);//toArgumentInputParameter(argument, a));
        }
        return argumentObjects;
    }

    private Object getArgument(DataFetchingEnvironment dfe, Field f) {
        // TODO: Only use source if argument ist annotated as source
        //        if (a.getAnnotations().containsOneOfTheseKeys(Annotations.SOURCE)) {
        //            Object source = dfe.getSource();
        //            if (source != null) {
        //                return source;
        //            }
        //        }

        Object argument = dfe.getArgument(f.getJavaName());
        if (argument != null) {
            return argument;
        }
        return null;
    }

    //    private Object toArgumentInputParameter(Object argumentValue, Argument a) throws GraphQLException {
    //        Type type = a.getType();
    //
    //        if (argumentValue != null) {
    //            Type.Kind kind = type.kind();
    //            if (kind.equals(Type.Kind.PRIMITIVE)) {
    //                return handlePrimative(argumentValue, a);
    //            } else if (kind.equals(Type.Kind.ARRAY)) {
    //                return handleArray(argumentValue, a);
    //            } else if (Classes.isOptional(type)) {
    //                return handleOptional(argumentValue);
    //            } else if (kind.equals(Type.Kind.PARAMETERIZED_TYPE)) {
    //                return handleCollection(argumentValue, a);
    //            } else if (kind.equals(Type.Kind.CLASS)) {
    //                return handleClass(argumentValue, a);
    //            } else {
    //                return handleDefault(argumentValue, a, "Not sure what to do with this kind");
    //            }
    //        }
    //        return handleDefault(argumentValue, a, "Argument is NULL");
    //    }

    private Class[] getParameterClasses() {
        if (method.hasParameters()) {
            List<Class> cl = new LinkedList<>();
            for (Field argument : method.getParameters()) {
                Class<?> clazz = Classes.loadClass(argument.getTypeReference().getClassName());
                cl.add(clazz);
            }
            return cl.toArray(new Class[] {});
        }
        return null;
    }
}

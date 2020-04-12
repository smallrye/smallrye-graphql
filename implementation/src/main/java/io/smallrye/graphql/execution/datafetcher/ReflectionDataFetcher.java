package io.smallrye.graphql.execution.datafetcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

import org.eclipse.microprofile.graphql.GraphQLException;
import org.jboss.logging.Logger;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.cdi.CDIDelegate;
import io.smallrye.graphql.execution.error.GraphQLExceptionWhileDataFetching;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.x.Classes;
import io.smallrye.graphql.x.TransformException;
import io.smallrye.graphql.x.datafetcher.ReflectionDataFetcherException;

/**
 * Fetch data using CDI and Reflection
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReflectionDataFetcher implements DataFetcher {
    private static final Logger LOG = Logger.getLogger(ReflectionDataFetcher.class.getName());

    private final CDIDelegate cdiDelegate = CDIDelegate.delegate();

    private final Operation operation;
    private final Class[] parameterClasses;

    public ReflectionDataFetcher(Operation operation) {
        this.operation = operation;
        this.parameterClasses = getParameterClasses();
    }

    @Override
    public Object get(DataFetchingEnvironment dfe) throws Exception {
        Object declaringObject = cdiDelegate.getInstanceFromCDI(operation.getClassName());
        Class cdiClass = declaringObject.getClass();

        try {
            if (operation.hasArguments()) {
                Method m = cdiClass.getMethod(operation.getMethodName(), parameterClasses);
                return m.invoke(declaringObject, getArguments(dfe).toArray());
            } else {
                Method m = cdiClass.getMethod(operation.getMethodName());
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
                    return getPartialResult(dfe, graphQLException);
                } else {
                    throw (Exception) throwable;
                }
            }
        }
    }

    private ArrayList getArguments(DataFetchingEnvironment dfe) throws GraphQLException {
        ArrayList argumentObjects = new ArrayList();
        for (Field f : operation.getArguments()) {
            Object argument = getArgument(dfe, f);
            argumentObjects.add(toArgumentInputParameter(argument, f));
        }

        return argumentObjects;
    }

    private Object getArgument(DataFetchingEnvironment dfe, Field f) {
        // Only use source if argument is annotated as source
        //        if (f.isSource()) {
        //            Object source = dfe.getSource();
        //            if (source != null) {
        //                return source;
        //            }
        //        }

        Object argument = dfe.getArgument(f.getMethodName());

        if (argument != null) {
            return argument;
        }

        // Maybe there is a default ?
        return f.getDefaultValue().orElse(null);
    }

    // Make sure we get the correct class type
    private Object toArgumentInputParameter(Object argumentValue, Field f) throws GraphQLException {
        log(f);

        LOG.info("argumentValue class = " + argumentValue.getClass().getName());

        return argumentValue;

        //        if (argumentValue != null) {
        //            if(f.isCollection()){
        //                
        //            }else if (kind.equals(ReferenceType.SCALAR)) {
        //                return handlePrimative(argumentValue, f);
        //            } else if (kind.equals(ReferenceType.)) {
        //                return handleArray(argumentValue, a);
        //            } else if (Classes.isOptional(type)) {
        //                return handleOptional(argumentValue);
        //            } else if (kind.equals(Type.Kind.PARAMETERIZED_TYPE)) {
        //                return handleCollection(argumentValue, a);
        //            } else if (kind.equals(Type.Kind.CLASS)) {
        //                return handleClass(argumentValue, a);
        //            } else {
        //                return handleDefault(argumentValue, f, "Not sure what to do with this kind");
        //            }
        //        }
        //        return handleDefault(argumentValue, f, "Argument is NULL");
    }

    private Object handlePrimative(Object argumentValue, Field f) {
        // First make sure we have a primative type
        Class givenClass = argumentValue.getClass();
        if (!givenClass.isPrimitive()) {
            givenClass = Classes.toPrimativeClassType(givenClass);
        }
        if (givenClass.getName().equals(f.getReference().getClassName())) {
            return argumentValue; // Transformation here ?
        } else {
            return argumentValue;
            // TODO: return toScalar(argumentValue, a, clazz);
        }
    }

    //    private Object toScalar(Object input, Argument argument, Class clazz) {
    //        GraphQLScalarType scalar = getScalarType(argument.getType());
    //        if (scalar != null) {
    //            try {
    //                // For transformable scalars.
    //                if (Transformable.class.isInstance(scalar)) {
    //                    Transformable transformable = Transformable.class.cast(scalar);
    //                    input = transformable.transform(input, argument);
    //                }
    //                return Classes.stringToScalar(input.toString(), clazz);
    //
    //            } catch (NumberFormatException nfe) {
    //                throw new TransformException(nfe, scalar, argument.getName(), input.toString());
    //            }
    //        } else {
    //            return handleDefault(input, argument, "Expected type [" + clazz.getName() + "]");
    //        }
    //    }

    private Object handleDefault(Object argumentValue, Field f, String message) {
        if (argumentValue == null) {
            return null;
        }
        ReferenceType type = f.getReference().getType();
        LOG.warn(message + " | argument [" + argumentValue + "] of kind [" + argumentValue.getClass().getName()
                + "] but expecting kind [" + type.name() + "]");

        return argumentValue;
    }

    private Class[] getParameterClasses() {
        if (operation.hasArguments()) {
            List<Class> cl = new LinkedList<>();
            for (Field argument : operation.getArguments()) {
                Class<?> clazz = Classes.loadClass(argument.getReference().getClassName());
                cl.add(clazz);
            }
            return cl.toArray(new Class[] {});
        }
        return null;
    }

    private void log(Object o) {
        JsonbConfig config = new JsonbConfig().withFormatting(true);
        Jsonb jsonb = JsonbBuilder.create(config);
        String result = jsonb.toJson(o);
        LOG.info(result);
    }

    private DataFetcherResult<Object> getPartialResult(DataFetchingEnvironment dfe, GraphQLException graphQLException) {
        DataFetcherExceptionHandlerParameters handlerParameters = DataFetcherExceptionHandlerParameters
                .newExceptionParameters()
                .dataFetchingEnvironment(dfe)
                .exception(graphQLException)
                .build();

        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ExecutionPath path = handlerParameters.getPath();
        GraphQLExceptionWhileDataFetching error = new GraphQLExceptionWhileDataFetching(path, graphQLException,
                sourceLocation);

        return DataFetcherResult.newResult()
                .data(graphQLException.getPartialResults())
                .error(error)
                .build();

    }
}

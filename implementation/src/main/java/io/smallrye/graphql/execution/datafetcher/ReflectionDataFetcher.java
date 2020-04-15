package io.smallrye.graphql.execution.datafetcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.microprofile.graphql.GraphQLException;
import org.jboss.logging.Logger;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.cdi.CDIDelegate;
import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.execution.datafetcher.helper.ArgumentHelper;
import io.smallrye.graphql.execution.datafetcher.helper.FieldHelper;
import io.smallrye.graphql.execution.error.GraphQLExceptionWhileDataFetching;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Fetch data using CDI and Reflection
 * 
 * TODO: Add Metrics back
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReflectionDataFetcher implements DataFetcher {
    private static final Logger LOG = Logger.getLogger(ReflectionDataFetcher.class.getName());

    private final CDIDelegate cdiDelegate = CDIDelegate.delegate();

    private final Operation operation;
    private final Class[] parameterClasses;
    private final FieldHelper fieldHelper;
    private final ArgumentHelper argumentHelper;

    /**
     * We use this reflection data fetcher on operations (so Queries, Mutations and Source)
     * 
     * ParameterClasses: We need an Array of Classes that this operation method needs so we can use reflection to call the
     * method.
     * FieldHelper: We might have to transform the data on the way out if there was a Formatting annotation on the method,
     * or return object fields, we can not use normal JsonB to do this because we do not bind a full object, and we support
     * annotation that is not part on JsonB
     * 
     * ArgumentHelper: The same as above, except for every parameter on the way in.
     * 
     * @param operation the operation
     * 
     */
    public ReflectionDataFetcher(Operation operation) {
        this.operation = operation;
        this.parameterClasses = getParameterClasses();
        this.fieldHelper = new FieldHelper(operation);
        this.argumentHelper = new ArgumentHelper(operation.getArguments());
    }

    /**
     * This makes the call on the method. We do the following:
     * 1) Get the correct instance of the class we want to make the call in using CDI. That allow the developer to still use
     * Scopes in the bean.
     * 2) Get the argument values (if any) from graphql-java and make sue they are in the correct type, and if needed,
     * transformed.
     * 3) Make a call on the method with the correct arguments
     * 4) get the result and if needed transform it before we return it.
     * 
     * @param dfe the Data Fetching Environment from graphql-java
     * @return the result from the call.
     * 
     * @throws Exception
     */
    @Override
    public Object get(DataFetchingEnvironment dfe) throws Exception {
        Object declaringObject = cdiDelegate.getInstanceFromCDI(operation.getClassName());
        Class cdiClass = declaringObject.getClass();

        try {
            Object resultFromMethodCall = null;
            if (operation.hasArguments()) {
                Method m = cdiClass.getMethod(operation.getMethodName(), parameterClasses);
                List transformedArguments = argumentHelper.getArguments(dfe);
                validateArgumentTypes(transformedArguments);
                resultFromMethodCall = m.invoke(declaringObject, transformedArguments.toArray());
            } else {
                Method m = cdiClass.getMethod(operation.getMethodName());
                resultFromMethodCall = m.invoke(declaringObject);
            }

            // See if we need to transform on the way out
            return fieldHelper.transformResponse(resultFromMethodCall);

        } catch (InvocationTargetException | NoSuchMethodException | SecurityException | GraphQLException
                | IllegalAccessException | IllegalArgumentException ex) {
            Throwable throwable = ex.getCause();

            if (throwable == null) {
                throw new DataFetcherException(operation, ex);
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

    //    private Optional<GraphQLScalarType> getExpectedScalarType(Argument argument) {
    //        String expectedPrimitiveClassName = argument.getReference().getClassName();
    //        if (GraphQLScalarTypes.getScalarMap().containsKey(expectedPrimitiveClassName)) {
    //            return Optional.of(GraphQLScalarTypes.getScalarMap().get(expectedPrimitiveClassName));
    //        }
    //        return Optional.empty();
    //    }
    //
    //    private Object logMismatchAndReturnWrongType(Object argumentValue, Argument argument, String message) {
    //        //LOG.warn(message + " | argument [" + argumentValue + "] of kind [" + argumentValue.getClass().getName()
    //        //        + "] but expecting kind [" + argument.getReference().getClassName() + "]");
    //        return argumentValue;
    //    }

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

    /**
     * This created an array of classes needed when calling the method using reflection for argument input.
     * This gets created on construction
     * 
     * @return the array of classes.
     */
    private Class[] getParameterClasses() {
        if (operation.hasArguments()) {
            List<Class> cl = new LinkedList<>();
            for (Field argument : operation.getArguments()) {
                // If the argument is an array / collection, load that class
                if (argument.getArray().isPresent()) {
                    Class<?> clazz = Classes.loadClass(argument.getArray().get().getClassName());
                    cl.add(clazz);
                } else {
                    Class<?> clazz = Classes.loadClass(argument.getReference().getClassName());
                    cl.add(clazz);
                }
            }
            return cl.toArray(new Class[] {});
        }
        return null;
    }

    /**
     * Might be used for debugging. This check that all arguments is in the correct type
     * 
     * @param arguments
     */
    private void validateArgumentTypes(List arguments) {
        Stack<Argument> argumentStack = new Stack();
        argumentStack.addAll(operation.getArguments());
        Stack receivedStack = new Stack();
        receivedStack.addAll(arguments);
        for (Class c : parameterClasses) {
            Argument argument = argumentStack.pop();
            String expected = c.getName();
            String received = receivedStack.pop().getClass().getName();
            if (!expected.equals(received)) {
                LOG.warn("Wrong argument type in " + argument.getMethodArgumentName() + "!"
                        + "\n\t Expected = [" + expected + "]"
                        + "\n\t Received = [" + received + "]");
            }
        }
    }
}

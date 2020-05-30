package io.smallrye.graphql.execution.datafetcher.helper;

import java.util.List;
import java.util.Map;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbException;

import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.SmallRyeGraphQLServerLogging;
import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.json.InputTransformFields;
import io.smallrye.graphql.json.JsonBCreator;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.transformation.DataFetchingException;
import io.smallrye.graphql.transformation.TransformException;
import io.smallrye.graphql.transformation.Transformer;

/**
 * Help with the arguments when doing reflection calls
 *
 * Here we need to transform (if needed) the arguments, and then make sure we
 * get the in the correct class type as expected by the method we want to call.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ArgumentHelper extends AbstractHelper {

    private final List<Argument> arguments;

    /**
     * We need the modeled arguments to create the correct values
     *
     * @param arguments the arguments
     *
     */
    public ArgumentHelper(List<Argument> arguments) {
        this.arguments = arguments;
    }

    /**
     * This gets a list of arguments that we need to all the method.
     *
     * We need to make sure the arguments is in the correct class type and,
     * if needed, transformed
     *
     * @param dfe the Data Fetching Environment from graphql-java
     *
     * @return a (ordered) List of all argument values
     */
    public Object[] getArguments(DataFetchingEnvironment dfe) throws DataFetchingException {
        Object[] argumentObjects = new Object[arguments.size()];
        int idx = 0;
        for (Argument argument : arguments) {
            Object argumentValue = getArgument(dfe, argument);
            argumentObjects[idx] = argumentValue;
            idx++;
        }

        return argumentObjects;
    }

    /**
     * Get one argument.
     *
     * As with above this argument needs to be transformed and in the correct class type
     *
     * @param dfe the Data Fetching Environment from graphql-java
     * @param argument the argument (as created while building the model)
     * @return the value of the argument
     */
    private Object getArgument(DataFetchingEnvironment dfe, Argument argument) throws DataFetchingException {
        // If this is a source argument, just return the source. The source does 
        // not need transformation and would already be in the correct class type
        if (argument.isSourceArgument()) {
            Object source = dfe.getSource();
            if (source != null) {
                return source;
            }
        }

        // Else, get the argument value as if is from graphql-java
        // graphql-java will also populate the value with the default value if needed.
        Object argumentValueFromGraphQLJava = dfe.getArgument(argument.getName());

        // return null if the value is null
        if (argumentValueFromGraphQLJava == null) {
            return null;
        }

        return super.recursiveTransform(argumentValueFromGraphQLJava, argument);
    }

    /**
     * By now this is a 'leaf' value, i.e not a collection of array, so we just transform if needed.
     * the result might be in the wrong format.
     *
     * @param argumentValue the value to transform
     * @param field the field as created while scanning
     * @return transformed value
     */
    @Override
    Object singleTransform(Object argumentValue, Field field) throws DataFetchingException {
        return Transformer.in(field, argumentValue);
    }

    /**
     * Here we have the potential transformed input and just need to
     * get the correct type
     *
     * @param fieldValue the input from graphql-java, potentially transformed
     * @param field the field as created while scanning
     * @return the value to use in the method call
     */
    @Override
    protected Object afterRecursiveTransform(Object fieldValue, Field field) throws DataFetchingException {
        String expectedType = field.getReference().getClassName();
        String receivedType = fieldValue.getClass().getName();

        // No need to do anything, everyting is already correct
        if (expectedType.equals(receivedType)) {
            return fieldValue;
        } else if (Classes.isPrimitiveOf(expectedType, receivedType)) {
            //expected is a primitive, we got the wrapper
            return fieldValue;
        } else {
            return correctObjectClass(fieldValue, field);
        }
    }

    /**
     * Here we create a Object from the input.
     * This can be a complex POJO input, or a default value set by graphql-java or a Scalar (that is not a primitive, like Date)
     *
     * @param argumentValue the argument from graphql-java
     * @param field the field as created while scanning
     * @return the return value
     */
    private Object correctObjectClass(Object argumentValue, Field field) throws DataFetchingException {
        String receivedClassName = argumentValue.getClass().getName();

        if (Map.class.isAssignableFrom(argumentValue.getClass())) {
            return correctComplexObjectFromMap((Map) argumentValue, field);
        } else if (receivedClassName.equals(String.class.getName())) {
            // We got a String, but not expecting one. Lets bind to Pojo with JsonB
            // This happens with @DefaultValue and Transformable (Passthrough) Scalars
            return correctComplexObjectFromJsonString(argumentValue.toString(), field);
        } else {
            SmallRyeGraphQLServerLogging.log.dontKnowHoToHandleArgument(field.getMethodName());
            return argumentValue;
        }
    }

    /**
     * If we got a map from graphql-java, this is a complex pojo input object
     *
     * We need to create a object from this using JsonB.
     * We also need to handle transformation of fields that is on this complex type.
     *
     * The transformation with JsonB annotation will happen when binding, and the transformation
     * with non-jsonb annotatin will happen when we create a json string from the map.
     *
     * @param m the map from graphql-java
     * @param field the field as created while scanning
     * @return a java object of this type.
     */
    private Object correctComplexObjectFromMap(Map m, Field field) throws DataFetchingException {
        String className = field.getReference().getClassName();

        // Let's see if there are any fields that needs transformation
        if (InputTransformFields.hasTransformationFields(className)) {
            Map<String, Field> transformationFields = InputTransformFields.getTransformationFields(className);

            for (Map.Entry<String, Field> entry : transformationFields.entrySet()) {
                String fieldName = entry.getKey();
                if (m.containsKey(fieldName)) {
                    Object valueThatShouldTransform = m.get(fieldName);
                    Field fieldThatShouldTransform = entry.getValue();
                    valueThatShouldTransform = recursiveTransform(valueThatShouldTransform, fieldThatShouldTransform);
                    m.put(fieldName, valueThatShouldTransform);
                }
            }
        }

        // Create a valid jsonString from a map    
        String jsonString = JsonBCreator.getJsonB().toJson(m);
        return correctComplexObjectFromJsonString(jsonString, field);
    }

    /**
     * This is used once we have a valid jsonString, either from above or from complex default value from graphql-java
     *
     * @param jsonString the object represented as a json String
     * @param field the field as created while scanning
     * @return the correct object
     */
    private Object correctComplexObjectFromJsonString(String jsonString, Field field) throws DataFetchingException {
        Class ownerClass = classloadingService.loadClass(field.getReference().getClassName());
        try {
            Jsonb jsonb = JsonBCreator.getJsonB(field.getReference().getClassName());
            return jsonb.fromJson(jsonString, ownerClass);
        } catch (JsonbException jbe) {
            throw new TransformException(jbe, field, jsonString);
        }
    }

}

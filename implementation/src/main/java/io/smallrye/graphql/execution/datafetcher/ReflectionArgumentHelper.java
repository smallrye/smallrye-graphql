package io.smallrye.graphql.execution.datafetcher;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.bind.Jsonb;

import org.eclipse.microprofile.graphql.GraphQLException;
import org.jboss.logging.Logger;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.json.InputTransformFields;
import io.smallrye.graphql.json.JsonBCreator;
import io.smallrye.graphql.scalar.GraphQLScalarTypes;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Operation;

/**
 * Help with the arguments when doing reflection calls
 * 
 * Here we need to transform (if needed) the arguments, and then make sure we
 * get the in the correct class type as expected by the method we want to call.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ReflectionArgumentHelper {
    private static final Logger LOG = Logger.getLogger(ReflectionArgumentHelper.class.getName());

    private final Operation operation;

    /**
     * We need the operation to get the modeled arguments
     * 
     * @param operation the operation
     * 
     */
    public ReflectionArgumentHelper(Operation operation) {
        this.operation = operation;
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
     * 
     * @throws GraphQLException
     */
    public List getArguments(DataFetchingEnvironment dfe) throws GraphQLException {
        List argumentObjects = new LinkedList();
        for (Argument argument : operation.getArguments()) {
            Object argumentValue = getArgument(dfe, argument);
            argumentObjects.add(argumentValue);
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
     * @throws GraphQLException
     */
    private Object getArgument(DataFetchingEnvironment dfe, Argument argument) throws GraphQLException {
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

        return transformAndCorrectClassType(argumentValueFromGraphQLJava, argument);

    }

    /**
     * Here we actually do the transform and then correct the class.
     * 
     * This method get called recursively in the case of arrays
     * 
     * @param argumentValueFromGraphQLJava the value we got from graphql-java
     * @param field details about the expected type created while scanning the code
     * @return the argumentValue in the correct type and transformed
     */
    private Object transformAndCorrectClassType(Object argumentValueFromGraphQLJava, Field field)
            throws GraphQLException {

        String expectedType = field.getReference().getClassName();
        String receivedType = argumentValueFromGraphQLJava.getClass().getName();

        if (field.getArray().isPresent()) {
            // First handle the array if this is an array
            if (field.getArray().get().getType().equals(io.smallrye.graphql.schema.model.Array.Type.ARRAY)) {
                return transformAndCorrectArray(argumentValueFromGraphQLJava, field);
            } else {
                return transformAndCorrectCollection(argumentValueFromGraphQLJava, field);
            }
        } else if (Classes.isOptional(expectedType)) {
            // Also handle optionals
            return transformAndCorrectOptional(argumentValueFromGraphQLJava, field);
        } else if (expectedType.equals(receivedType) && !shouldTransform(field)) {
            // If the type is already correct, and there is no transformer
            return argumentValueFromGraphQLJava;
        } else if (shouldTransform(field)) {
            // we need to transform before we make sure the type is correct
            argumentValueFromGraphQLJava = transform(argumentValueFromGraphQLJava, field);
        }

        // Get the correct class to return
        return correctClassType(argumentValueFromGraphQLJava, field);

    }

    /**
     * This just creates a new array and add values to it by calling the transformAndCorrectClassType method.
     * This allows arrays of arrays and transformation inside arrays
     * Even without transformation, we need to go from arrayList to Array,
     * or arraylist of arraylist to array of array
     * 
     * @param <T> the type in the array
     * @param argumentValue the list as from graphql-java (always an arraylist)
     * @param field the field as created while scanning
     * @return an array with the transformed values in.
     * @throws GraphQLException
     */
    private <T> Object transformAndCorrectArray(Object argumentValue, Field field) throws GraphQLException {

        ArrayList givenArrayList = (ArrayList) argumentValue;

        List convertedList = new ArrayList();

        for (Object objectInGivenCollection : givenArrayList) {
            Field fieldInCollection = getFieldInField(field);
            Object objectInCollection = transformAndCorrectClassType(objectInGivenCollection,
                    fieldInCollection);
            convertedList.add(objectInCollection);
        }

        String classNameInCollection = field.getReference().getClassName();
        Class classInCollection = Classes.loadClass(classNameInCollection);

        return convertedList.toArray((T[]) Array.newInstance(classInCollection, givenArrayList.size()));
    }

    /**
     * This just creates a new correct type collection and add values to it by calling the transformAndCorrectClassType method.
     * This allows collections of collections and transformation inside collections
     * Even without transformation, we need to go from arrayList to the correct collection type,
     * or arraylist of arraylist to collection of collection
     * 
     * @param <T> the type in the collection
     * @param argumentValue the list as from graphql-java (always an arraylist)
     * @param field the field as created while scanning
     * @return a collection with the transformed values in.
     * @throws GraphQLException
     */
    private Object transformAndCorrectCollection(Object argumentValue, Field field) throws GraphQLException {
        ArrayList givenArrayList = (ArrayList) argumentValue;

        String collectionClassName = field.getArray().get().getClassName();

        Collection convertedCollection = CollectionHelper.newCollection(collectionClassName);

        for (Object objectInGivenCollection : givenArrayList) {
            Field fieldInCollection = getFieldInField(field);
            Object objectInCollection = transformAndCorrectClassType(objectInGivenCollection,
                    fieldInCollection);
            convertedCollection.add(objectInCollection);
        }

        return convertedCollection;
    }

    /**
     * This is not yet specified by MicroProfile GraphQL, but we support it by also allowing transformation the optional
     * element.
     * 
     * @param argumentValue the value as from graphql-java
     * @param argument the argument as created while scanning
     * @return a optional with the transformed value in.
     * @throws GraphQLException
     */
    private Object transformAndCorrectOptional(Object argumentValue, Field field) throws GraphQLException {
        // Check the type and maybe apply transformation
        if (argumentValue == null) {
            return Optional.empty();
        } else {
            Collection givenCollection = (Collection) argumentValue;
            if (givenCollection.isEmpty()) {
                return Optional.empty();
            } else {
                Object o = givenCollection.iterator().next();
                return Optional.of(transformAndCorrectClassType(o, field));
            }
        }
    }

    /**
     * Here we have the potential transformed input and just need to
     * get the correct type
     * 
     * @param input the input from graphql-java, potentially transformed
     * @param field the field as created while scanning
     * @return the value to use in the method call
     */
    private Object correctClassType(Object input, Field field) throws GraphQLException {
        String expectedType = field.getReference().getClassName();
        if (Classes.isPrimitive(expectedType)) {
            return correctPrimitiveClass(input, field);
        } else {
            return correctObjectClass(input, field);
        }
    }

    /**
     * Here we create a primitive from the input, and by now the input is transformed and we
     * just need to make sure the type is correct
     * 
     * @param input the input from graphql-java, potentially transformed
     * @param field the field as created while scanning
     * @return the value to use in the method call
     */
    private Object correctPrimitiveClass(Object input, Field field) {
        String receivedClass = input.getClass().toString();
        // We received a Object equivalent, let's box it 
        if (Classes.isPrimitive(receivedClass)) {
            Class correctType = Classes.toPrimativeClassType(input);
            return correctType.cast(input);
        } else {

            // TODO: Scalar to Primative ?
            // Find it from the toString value and create a new correct class.
            return Classes.stringToScalar(input.toString(), field.getReference().getClassName());
        }
    }

    /**
     * Here we create a Object from the input.
     * This can be a complex POJO input, or a default value set by graphql-java or a Scalar (that is not a primitive, like Date)
     * 
     * @param argumentValue the argument from graphql-java
     * @param field the field as created while scanning
     * @return the return value
     * @throws GraphQLException
     */
    private Object correctObjectClass(Object argumentValue, Field field) throws GraphQLException {
        String receivedClassName = argumentValue.getClass().getName();

        if (Map.class.isAssignableFrom(argumentValue.getClass())) {
            return correctComplexObjectFromMap(Map.class.cast(argumentValue), field);
        } else if (receivedClassName.equals(String.class.getName())) {
            // We got a String, but not expecting one. Lets bind to Pojo with JsonB
            // This happens with @DefaultValue and Transformable (Passthrough) Scalars
            return correctComplexObjectFromJsonString(argumentValue.toString(), field);
        } else if (GraphQLScalarTypes.isScalarType(field.getReference().getClassName())) {
            return correctScalarObjectFromString(argumentValue, field);
        } else {
            LOG.warn("Returning input argument as is, because we did not know how to handle it.\n\t"
                    + "[" + field.getMethodName() + "] in [" + operation.getClassName() + "]");
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
     * TODO: This is not going to work deeper into the object, we need to call this recursively
     * 
     * @param m the map from graphql-java
     * @param field the field as created while scanning
     * @return a java object of this type.
     * @throws GraphQLException
     */
    private Object correctComplexObjectFromMap(Map m, Field field) throws GraphQLException {
        String className = field.getReference().getClassName();

        // Let's see if there are any fields that needs transformation
        if (InputTransformFields.hasTransformationFields(className)) {
            Map<String, Field> transformationFields = InputTransformFields.getTransformationFields(className);

            for (Map.Entry<String, Field> entry : transformationFields.entrySet()) {
                String fieldName = entry.getKey();
                if (m.containsKey(fieldName)) {
                    Object valueThatShouldTransform = m.get(fieldName);
                    Field fieldThatShouldTransform = entry.getValue();
                    valueThatShouldTransform = transformAndCorrectClassType(valueThatShouldTransform, fieldThatShouldTransform);
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
    private Object correctComplexObjectFromJsonString(String jsonString, Field field) {
        Class ownerClass = Classes.loadClass(field.getReference().getClassName());

        Jsonb jsonb = JsonBCreator.getJsonB(field.getReference().getClassName());
        return jsonb.fromJson(jsonString, ownerClass);
    }

    /**
     * This create a value by using the GraphQL Scalar.
     * 
     * @param argumentValue the value from graphql-java
     * @param field the field as created while scanning
     * @return the Scalar value
     */
    private Object correctScalarObjectFromString(Object argumentValue, Field field) {
        String expectedClass = field.getReference().getClassName();
        GraphQLScalarType graphQLScalarType = GraphQLScalarTypes.getScalarMap().get(expectedClass);
        Object object = graphQLScalarType.getCoercing().serialize(argumentValue);
        return object;
    }

    /**
     * Here we create an field from a field, but reducing the collection depth.
     * This will happen until we get to the field in the collection.
     * 
     * This 'clone' an array, but update the array information
     * 
     * @param owner the owner field
     * @return the new field
     */
    private Field getFieldInField(Field owner) {
        Field child = new Field(owner.getMethodName(),
                owner.getPropertyName(),
                owner.getName(),
                owner.getDescription(),
                owner.getReference());
        // not null
        if (owner.isNotNull()) {
            child.markNotNull();
        }
        // transform info
        child.setTransformInfo(owner.getTransformInfo());
        // default value
        child.setDefaultValue(owner.getDefaultValue());

        // array
        if (owner.getArray().isPresent()) {
            io.smallrye.graphql.schema.model.Array ownerArray = owner.getArray().get();

            int depth = ownerArray.getDepth() - 1;
            if (depth > 0) {
                // We still not at the end
                io.smallrye.graphql.schema.model.Array childArray = new io.smallrye.graphql.schema.model.Array(
                        ownerArray.getClassName(),
                        ownerArray.getType(), depth);
                child.setArray(Optional.of(childArray));
            }
        }

        return child;

    }

    /**
     * Check if we need to transform
     * 
     * @param argument the argument model
     * @return true if we have to
     */
    private boolean shouldTransform(Field field) {
        return field.getTransformInfo().isPresent();
    }

    /**
     * By now this is a 'leaf' value, i.e not a collection of array, so we just transform if needed.
     * the result might be in the wrong format.
     * 
     * @param argumentValueFromGraphQLJava the value to transform
     * @param field the field as created while scanning
     * @return transformed value
     */
    private Object transform(Object argumentValueFromGraphQLJava, Field field) {
        if (shouldTransform(field)) {
            Transformer transformer = Transformer.transformer(field);
            return argumentValueFromGraphQLJava = transformer.parseInput(argumentValueFromGraphQLJava);
        } else {
            return argumentValueFromGraphQLJava;
        }
    }

}

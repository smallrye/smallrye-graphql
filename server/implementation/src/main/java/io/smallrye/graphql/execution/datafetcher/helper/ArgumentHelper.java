package io.smallrye.graphql.execution.datafetcher.helper;

import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbException;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.json.InputFieldsInfo;
import io.smallrye.graphql.json.JsonBCreator;
import io.smallrye.graphql.scalar.GraphQLScalarTypes;
import io.smallrye.graphql.schema.model.Adapter;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;
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
     * We need to make sure the arguments is in the correct class type and, if needed, transformed
     *
     * @param dfe the Data Fetching Environment from graphql-java
     *
     * @return a (ordered) List of all argument values
     * @throws io.smallrye.graphql.transformation.AbstractDataFetcherException
     */
    public Object[] getArguments(DataFetchingEnvironment dfe) throws AbstractDataFetcherException {
        return getArguments(dfe, false);
    }

    public Object[] getArguments(DataFetchingEnvironment dfe, boolean excludeSource) throws AbstractDataFetcherException {
        List<Object> argumentObjects = new LinkedList<>();
        for (Argument argument : arguments) {
            if (!argument.isSourceArgument() || !excludeSource) {
                Object argumentValue = getArgument(dfe, argument);
                argumentObjects.add(argumentValue);
            }
        }
        return argumentObjects.toArray();
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
    private Object getArgument(DataFetchingEnvironment dfe, Argument argument) throws AbstractDataFetcherException {
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
            if (argument.hasWrapper() && argument.getWrapper().isOptional()) {
                return Optional.empty();
            }
            return null;
        }

        // wrap in optional if the argument is optional
        if (argument.hasWrapper() && argument.getWrapper().isOptional()) {
            argumentValueFromGraphQLJava = Optional.of(argumentValueFromGraphQLJava);
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
    Object singleTransform(Object argumentValue, Field field) throws AbstractDataFetcherException {
        if (!shouldTransform(field)) {
            return argumentValue;
        } else {
            return transformInput(field, argumentValue);
        }
    }

    /**
     * By now this is a 'leaf' value, i.e not a collection of array, so we just map if needed.
     *
     * @param argumentValue the value to map
     * @param field the field as created while scanning
     * @return mapped value
     */
    @Override
    Object singleMapping(Object argumentValue, Field field) throws AbstractDataFetcherException {
        if (shouldApplyMapping(field)) {
            String methodName = getCreateMethodName(field);
            if (methodName != null && !methodName.isEmpty()) {
                Class<?> mappingClass = classloadingService.loadClass(field.getReference().getClassName());
                try {
                    if (methodName.equals(CONTRUCTOR_METHOD_NAME)) {
                        // Try with contructor
                        Constructor<?> constructor = mappingClass.getConstructor(argumentValue.getClass());
                        return constructor.newInstance(argumentValue);

                    } else {
                        // Try with method
                        Method method = mappingClass.getMethod(methodName, argumentValue.getClass());
                        if (Modifier.isStatic(method.getModifiers())) {
                            Object instance = method.invoke(null, argumentValue);
                            return instance;
                        } else { // We need an instance, so assuming a public no-arg contructor
                            Constructor<?> constructor = mappingClass.getConstructor();
                            Object instance = constructor.newInstance();
                            method.invoke(instance, argumentValue);
                            return instance;
                        }
                    }
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException ex) {
                    ex.printStackTrace();
                }
            }
        }
        // Fall back to the original value
        return argumentValue;
    }

    private Object transformInput(Field field, Object object) throws AbstractDataFetcherException {
        if (object == null) {
            return null;
        }
        if (!shouldTransform(field)) {
            return object;
        }

        if (field.hasAdapter()) {
            return transformInputWithAdapter(field, object);
        } else {
            return transformInputWithTransformer(field, object);
        }
    }

    /**
     * This is for when a user provided a adapter.
     * 
     * @param field the field definition
     * @param object the pre transform value
     * @return the transformed value
     * @throws AbstractDataFetcherException
     */
    private Object transformInputWithAdapter(Field field, Object object) throws AbstractDataFetcherException {

        if (!field.getAdapter().isJsonB()) { // We use JsonB internally, so we can skip for JsonB
            Adapter adapter = field.getAdapter();
            ReflectionInvoker reflectionInvoker = getReflectionInvokerForInput(adapter);
            try {
                if (Map.class.isAssignableFrom(object.getClass())) { // For complex object we receive a map from graphql-java
                    object = correctComplexObjectFromMap((Map) object, field);
                }
                Object adaptedObject = reflectionInvoker.invoke(object);
                return adaptedObject;
            } catch (Exception ex) {
                log.transformError(ex);
                throw new TransformException(ex, field, object);
            }
        }
        return object;
    }

    /**
     * This is the build in transformation (eg. number and date formatting)
     * 
     * @param field the field definition
     * @param object the pre transform value
     * @return the transformed value
     * @throws AbstractDataFetcherException
     */
    private Object transformInputWithTransformer(Field field, Object object) throws AbstractDataFetcherException {
        try {
            Transformer transformer = super.getTransformer(field);
            if (transformer == null) {
                return object;
            }
            return transformer.in(object);
        } catch (Exception e) {
            throw new TransformException(e, field, object);
        }
    }

    private boolean shouldApplyMapping(Field field) {
        return field.getReference().hasMapping()
                && field.getReference().getMapping().getDeserializeMethod() != null
                ||
                field.hasMapping()
                        && field.getMapping().getDeserializeMethod() != null;
    }

    private String getCreateMethodName(Field field) {
        if (field.getReference().hasMapping()) {
            return field.getReference().getMapping().getDeserializeMethod();
        } else if (field.hasMapping()) {
            return field.getMapping().getDeserializeMethod();
        }
        return null;
    }

    /**
     * Here we have the potential transformed input and just need to
     * get the correct type
     *
     * @param fieldValue the input from graphql-java, potentially transformed
     * @param field the field as created while scanning
     * @return the value to use in the method call
     * @throws io.smallrye.graphql.transformation.AbstractDataFetcherException
     */
    @Override
    protected Object afterRecursiveTransform(Object fieldValue, Field field) throws AbstractDataFetcherException {
        String expectedType = field.getReference().getClassName();
        String receivedType = fieldValue.getClass().getName();

        // No need to do anything, everyting is already correct
        if (expectedType.equals(receivedType)) {
            return fieldValue;
        } else if (Classes.isPrimitiveOf(expectedType, receivedType)) {
            //expected is a primitive, we got the wrapper
            return fieldValue;
        } else if (field.getReference().getType().equals(ReferenceType.ENUM)) {
            Class<?> enumClass = classloadingService.loadClass(field.getReference().getClassName());
            return Enum.valueOf((Class<Enum>) enumClass, fieldValue.toString());
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
    private Object correctObjectClass(Object argumentValue, Field field) throws AbstractDataFetcherException {
        String receivedClassName = argumentValue.getClass().getName();

        if (Map.class.isAssignableFrom(argumentValue.getClass())) {
            return correctComplexObjectFromMap((Map) argumentValue, field);
        } else if (receivedClassName.equals(String.class.getName())) {
            // We got a String, but not expecting one. Lets bind to Pojo with JsonB
            // This happens with @DefaultValue and Transformable (Passthrough) Scalars
            return correctComplexObjectFromJsonString(argumentValue.toString(), field);
        } else if (!field.hasAdapter() && GraphQLScalarTypes.isGraphQLScalarType(field.getReference().getClassName())) {
            GraphQLScalarType scalar = GraphQLScalarTypes.getScalarByClassName(field.getReference().getClassName());
            return scalar.getCoercing().parseLiteral(argumentValue);
        } else if (!field.hasAdapter()) {
            log.dontKnowHoToHandleArgument(argumentValue.getClass().getName(), field.getMethodName());
        }
        return argumentValue;
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
    private Object correctComplexObjectFromMap(Map m, Field field) throws AbstractDataFetcherException {
        String className = field.getReference().getClassName();

        // Let's see if there are any fields that needs transformation or adaption
        if (InputFieldsInfo.hasTransformationFields(className)) {
            Map<String, Field> transformationFields = InputFieldsInfo.getTransformationFields(className);

            for (Map.Entry<String, Field> entry : transformationFields.entrySet()) {
                String fieldName = entry.getKey();
                if (m.containsKey(fieldName)) {
                    Object valueThatShouldTransform = m.get(fieldName);
                    Field fieldThatShouldTransform = entry.getValue();
                    valueThatShouldTransform = super.recursiveTransform(valueThatShouldTransform, fieldThatShouldTransform);
                    m.put(fieldName, valueThatShouldTransform);
                }
            }
        }

        // Let's see if there are any fields that needs mapping
        if (InputFieldsInfo.hasMappingFields(className)) {
            Map<String, Field> mappingFields = InputFieldsInfo.getMappingFields(className);

            for (Map.Entry<String, Field> entry : mappingFields.entrySet()) {
                String fieldName = entry.getKey();
                if (m.containsKey(fieldName)) {
                    Object valueThatShouldMap = m.get(fieldName);
                    Field fieldThatShouldMap = entry.getValue();
                    valueThatShouldMap = recursiveMapping(valueThatShouldMap, fieldThatShouldMap);
                    m.put(fieldName, valueThatShouldMap);
                }
            }
        }

        // make sure, all fields required by creator-method are set
        for (final String s : InputFieldsInfo.getCreatorParameters(className)) {
            // null should be safe, since primitive fields are already set (since marked as non null)
            m.putIfAbsent(s, null);
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
    private Object correctComplexObjectFromJsonString(String jsonString, Field field) throws AbstractDataFetcherException {
        Type type = getType(field.getReference());

        try {
            Jsonb jsonb = JsonBCreator.getJsonB(field.getReference().getClassName());
            return jsonb.fromJson(jsonString, type);
        } catch (JsonbException jbe) {
            throw new TransformException(jbe, field, jsonString);
        }
    }

    /**
     * Build the (possible generic) type for this reference.
     * 
     * @param reference the reference
     * @return the type
     */
    private Type getType(Reference reference) {
        Class<?> ownerClass = classloadingService.loadClass(reference.getClassName());
        if (reference.getParametrizedTypeArguments() == null
                || reference.getParametrizedTypeArguments().isEmpty()) {
            return ownerClass;
        }

        List<Type> typeParameters = new ArrayList<>();
        for (final TypeVariable<?> typeParameter : ownerClass.getTypeParameters()) {
            final Reference typeRef = reference.getParametrizedTypeArguments().get(typeParameter.getName());
            typeParameters.add(getType(typeRef));
        }
        final Type[] types = typeParameters.toArray(new Type[0]);

        return new ParameterizedTypeImpl(ownerClass, types);
    }

    private static class ParameterizedTypeImpl implements ParameterizedType {
        private final Type ownerClass;
        private final Type[] types;

        public ParameterizedTypeImpl(final Type ownerClass, final Type[] types) {
            this.ownerClass = ownerClass;
            this.types = types;
        }

        @Override
        public Type getRawType() {
            return ownerClass;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        @Override
        public Type[] getActualTypeArguments() {
            return types;
        }
    }

    private static final String CONTRUCTOR_METHOD_NAME = "<init>";
}

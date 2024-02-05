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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbException;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.json.InputFieldsInfo;
import io.smallrye.graphql.json.JsonBCreator;
import io.smallrye.graphql.scalar.GraphQLScalarTypes;
import io.smallrye.graphql.schema.model.AdaptWith;
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
    public List<Object> getArguments(DataFetchingEnvironment dfe) throws AbstractDataFetcherException {
        return getArguments(dfe, false);
    }

    public List<Object> getArguments(DataFetchingEnvironment dfe, boolean excludeSource) throws AbstractDataFetcherException {
        List<Object> argumentObjects = new LinkedList<>();
        for (Argument argument : arguments) {
            if (!argument.isSourceArgument() || !excludeSource) {
                Object argumentValue = getArgument(dfe, argument);
                argumentObjects.add(argumentValue);
            }
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

        return transformOrAdapt(argumentValueFromGraphQLJava, argument, dfe);
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
     * By now this is a 'leaf' value, i.e not a collection of array, so we just adapt if needed.
     *
     * @param argumentValue the value to map
     * @param field the field as created while scanning
     * @return mapped value
     */
    @Override
    Object singleAdapting(Object argumentValue, Field field, DataFetchingEnvironment dfe) throws AbstractDataFetcherException {
        if (argumentValue == null) {
            return null;
        }

        if (shouldAdaptWith(field)) {
            return adaptInputWith(field, argumentValue, dfe);
        } else if (shouldAdaptTo(field)) {
            return adaptInputTo(field, argumentValue);
        } else if (field.hasWrapper() && field.getWrapper().isMap()) {
            return defaultAdaptMap(field, argumentValue, dfe);
        }
        // Fall back to the original value
        return argumentValue;
    }

    private Object adaptInputTo(Field field, Object object) {
        String methodName = getCreateMethodName(field);
        if (methodName != null && !methodName.isEmpty()) {
            Class<?> mappingClass = classloadingService.loadClass(field.getReference().getClassName());
            try {
                if (methodName.equals(CONTRUCTOR_METHOD_NAME)) {
                    // Try with contructor
                    Constructor<?> constructor = mappingClass.getConstructor(object.getClass());
                    return constructor.newInstance(object);

                } else {
                    // Try with method
                    Method method = mappingClass.getMethod(methodName, object.getClass());
                    if (Modifier.isStatic(method.getModifiers())) {
                        Object instance = method.invoke(null, object);
                        return instance;
                    } else { // We need an instance, so assuming a public no-arg contructor
                        Constructor<?> constructor = mappingClass.getConstructor();
                        Object instance = constructor.newInstance();
                        method.invoke(instance, object);
                        return instance;
                    }
                }
            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                    | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }
        return object;
    }

    private Object defaultAdaptMap(Field field, Object argumentValue, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {
        Set providedSet = (Set) argumentValue;
        Set adaptInnerSet = (Set) recursiveAdapting(providedSet, mapAdapter.getAdaptedField(field), dfe);
        return mapAdapter.from(adaptInnerSet, field);
    }

    private Object adaptInputWith(Field field, Object argumentValue, DataFetchingEnvironment dfe)
            throws TransformException, AbstractDataFetcherException {
        if (argumentValue == null) {
            return null;
        }

        if (field.isAdaptingWith()) {
            AdaptWith adaptWith = field.getAdaptWith();
            ReflectionInvoker reflectionInvoker = getReflectionInvokerForInput(adaptWith);

            if (Map.class.isAssignableFrom(argumentValue.getClass())) {
                argumentValue = correctComplexObjectFromMap((Map) argumentValue, field, dfe);
            }

            try {
                Object adaptedObject = reflectionInvoker.invoke(argumentValue);
                return adaptedObject;
            } catch (Exception ex) {
                log.transformError(ex);
                throw new TransformException(ex, field, argumentValue);
            }
        }
        return argumentValue;
    }

    private Object transformInput(Field field, Object object) throws AbstractDataFetcherException {
        if (object == null) {
            return null;
        }
        if (!shouldTransform(field)) {
            return object;
        }

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

    private String getCreateMethodName(Field field) {
        if (field.getReference().isAdaptingTo()) {
            return field.getReference().getAdaptTo().getDeserializeMethod();
        } else if (field.isAdaptingTo()) {
            return field.getAdaptTo().getDeserializeMethod();
        }
        return null;
    }

    /**
     * Here we have the potential transformed input and just need to
     * get the correct type
     *
     * @param fieldValue the input from graphql-java, potentially transformed
     * @param field the field as created while scanning
     * @param dfe DataFetchingEnvironment from graphql-java
     * @return the value to use in the method call
     * @throws io.smallrye.graphql.transformation.AbstractDataFetcherException
     */
    @Override
    protected Object afterRecursiveTransform(Object fieldValue, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {
        String expectedType = field.getReference().getClassName();
        String receivedType = fieldValue.getClass().getName();

        // No need to do anything, everyting is already correct
        if (expectedType.equals(receivedType)) {
            return fieldValue;
        } else if (Classes.isPrimitiveOf(expectedType, receivedType)) {
            //expected is a primitive, we got the wrapper
            return fieldValue;
        } else if (expectedType.equals("java.util.Calendar") &&
                receivedType.equals("java.util.GregorianCalendar")) {
            // special case since in the CalendarTransformer#in it creates 'java.util.GregorianCalendar' object
            // if the argument is type of 'java.util.Calendar'
            return fieldValue;
        } else if (field.getReference().getType().equals(ReferenceType.ENUM)) {
            Class<?> enumClass = classloadingService.loadClass(field.getReference().getClassName());
            return Enum.valueOf((Class<Enum>) enumClass, fieldValue.toString());
        } else {
            return correctObjectClass(fieldValue, field, dfe);
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
    private Object correctObjectClass(Object argumentValue, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {
        String receivedClassName = argumentValue.getClass().getName();

        if (Map.class.isAssignableFrom(argumentValue.getClass())) {
            return correctComplexObjectFromMap((Map) argumentValue, field, dfe);
        } else if (receivedClassName.equals(String.class.getName())) {
            // Edge case for ObjectId: If the field is of type org.bson.types.ObjectId, return the argument value.
            // We need to handle ObjectId separately to avoid JSON-B calling its no-arg constructor, which would
            // generate a new, random ID. By returning the argument value as is, we preserve the original ObjectId.
            if (field.getReference().getClassName().equals("org.bson.types.ObjectId")) {
                return argumentValue;
            }
            // We got a String, but not expecting one. Lets bind to Pojo with JsonB
            // This happens with @DefaultValue and Transformable (Passthrough) Scalars
            return correctComplexObjectFromJsonString(argumentValue.toString(), field);
        } else if (GraphQLScalarTypes.isGraphQLScalarType(field.getReference().getClassName())) {
            GraphQLScalarType scalar = GraphQLScalarTypes.getScalarByClassName(field.getReference().getClassName());
            return scalar.getCoercing().parseLiteral(argumentValue);
        } else {
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
    private Object correctComplexObjectFromMap(Map m, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {
        String className = field.getReference().getClassName();

        // Let's see if there are any fields that needs transformation or adaption
        if (InputFieldsInfo.hasTransformationFields(className)) {
            Map<String, Field> transformationFields = InputFieldsInfo.getTransformationFields(className);

            for (Map.Entry<String, Field> entry : transformationFields.entrySet()) {
                String fieldName = entry.getKey();
                if (m.containsKey(fieldName)) {
                    Object valueThatShouldTransform = m.get(fieldName);
                    Field fieldThatShouldTransform = entry.getValue();
                    valueThatShouldTransform = super.recursiveTransform(valueThatShouldTransform, fieldThatShouldTransform,
                            dfe);
                    m.put(fieldName, valueThatShouldTransform);
                }
            }
        }

        // Let's see if there are any fields that needs adapting
        if (InputFieldsInfo.hasAdaptToFields(className)) {
            Map<String, Field> mappingFields = InputFieldsInfo.getAdaptToFields(className);

            for (Map.Entry<String, Field> entry : mappingFields.entrySet()) {
                String fieldName = entry.getKey();
                if (m.containsKey(fieldName)) {
                    Object valueThatShouldMap = m.get(fieldName);
                    Field fieldThatShouldMap = entry.getValue();
                    valueThatShouldMap = super.recursiveAdapting(valueThatShouldMap, fieldThatShouldMap, dfe);
                    m.put(fieldName, valueThatShouldMap);
                }
            }
        }

        if (InputFieldsInfo.hasAdaptWithFields(className)) {
            Map<String, Field> adaptingFields = InputFieldsInfo.getAdaptWithFields(className);

            for (Map.Entry<String, Field> entry : adaptingFields.entrySet()) {
                String fieldName = entry.getKey();
                if (m.containsKey(fieldName)) {
                    Object valueThatShouldAdapt = m.get(fieldName);
                    Field fieldThatShouldAdapt = entry.getValue();
                    Object valueThatAdapted = super.recursiveAdapting(valueThatShouldAdapt, fieldThatShouldAdapt, dfe);
                    m.put(fieldName, valueThatAdapted);
                }
            }
        }

        // make sure all fields required by creator-method are set
        m = includeNullCreatorParameters(m, field);

        // Create a valid jsonString from a map
        String jsonString = JsonBCreator.getJsonB(className).toJson(m);
        return correctComplexObjectFromJsonString(jsonString, field);
    }

    /**
     * Recursively add null fields for creator parameters which are not present in this map.
     * This is required by Yasson to be able to deserialize an object from the map.
     */
    private Map includeNullCreatorParameters(Map m, Field field) {
        if (m == null) {
            return null;
        }
        String className = field.getReference().getClassName();
        Map result = new HashMap(m);
        for (final Field f : InputFieldsInfo.getCreatorParameters(className)) {
            String s = f.getName();
            if (result.containsKey(s)) {
                Object fieldValue = result.get(s);
                if (fieldValue instanceof Map) {
                    result.put(s, includeNullCreatorParameters((Map) result.get(s), f));
                } else if (fieldValue instanceof List) {
                    result.put(s, handleList((List) fieldValue, f));

                }
            } else {
                result.put(s, null);
            }
        }
        return result;
    }

    private List handleList(List list, Field field) {
        List result = new ArrayList();
        list.forEach(item -> {
            if (item instanceof Map) {
                result.add(includeNullCreatorParameters((Map) item, field));
            } else {
                result.add(item);
            }
        });
        return result;
    }

    /**
     * This is used once we have a valid jsonString, either from above or from complex default value from graphql-java
     *
     * @param jsonString the object represented as a json String
     * @param field the field as created while scanning
     * @return the correct object
     */
    private Object correctComplexObjectFromJsonString(String jsonString, Field field) throws AbstractDataFetcherException {
        Type type;
        String className;
        if (field.isAdaptingWith()) {
            className = field.getAdaptWith().getToReference().getClassName();
            type = getType(field.getAdaptWith().getToReference());
        } else {
            type = getType(field.getReference());
            className = field.getReference().getClassName();
        }

        try {
            Jsonb jsonb = JsonBCreator.getJsonB(className);
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
        if (reference.getAllParametrizedTypes() == null
                || reference.getAllParametrizedTypes().isEmpty()) {
            return ownerClass;
        }

        List<Type> typeParameters = new ArrayList<>();
        for (final TypeVariable<?> typeParameter : ownerClass.getTypeParameters()) {
            final Reference typeRef = reference.getClassParametrizedType(typeParameter.getName());
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

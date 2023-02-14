package io.smallrye.graphql.execution.datafetcher.helper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.api.Entry;
import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.execution.datafetcher.CollectionCreator;
import io.smallrye.graphql.schema.model.AdaptWith;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Wrapper;
import io.smallrye.graphql.spi.ClassloadingService;
import io.smallrye.graphql.transformation.AbstractDataFetcherException;
import io.smallrye.graphql.transformation.Transformer;

/**
 * Help with the fields when fetching data.
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractHelper {

    protected final ClassloadingService classloadingService = ClassloadingService.get();
    protected final DefaultMapAdapter mapAdapter = new DefaultMapAdapter();
    private final Map<String, Transformer> transformerMap = new HashMap<>();
    private final Map<Integer, ReflectionInvoker> invokerMap = new HashMap<>();

    protected AbstractHelper() {
    }

    /**
     * Checks, if this field is a scalar and the object has the wrong type.
     * Transformation is only possible for scalars and only needed if types don't match.
     *
     * @param field the field
     * @return if transformation is needed
     */
    protected boolean shouldTransform(Field field) {
        return (field.getReference().getType() == ReferenceType.SCALAR
                && !field.getReference().getClassName().equals(field.getReference().getGraphQLClassName()));
    }

    /**
     * Checks if we should adapt the field
     * This is (for now) only applying to AdaptWith
     *
     * @param field the field
     * @return if adaption is needed
     */
    protected boolean shouldAdapt(Field field) {
        return shouldAdaptWith(field) || shouldAutoAdaptWithMap(field) || shouldAdaptTo(field);
    }

    protected boolean shouldAutoAdaptWithMap(Field field) {
        return field.hasWrapper() && field.getWrapper().isMap();
    }

    protected boolean shouldAdaptWith(Field field) {
        return field.getReference().isAdaptingWith() || field.isAdaptingWith();
    }

    protected boolean shouldAdaptTo(Field field) {
        return field.getReference().isAdaptingTo()
                && field.getReference().getAdaptTo().getDeserializeMethod() != null
                ||
                field.isAdaptingTo()
                        && field.getAdaptTo().getDeserializeMethod() != null;
    }

    public Object transformOrAdapt(Object val, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {

        val = recursiveTransform(val, field, dfe);
        if (shouldAdapt(field)) {
            val = recursiveAdapting(val, field, dfe);
        }

        return val;
    }

    /**
     * This gets called after the value has been recursively transformed.
     *
     * @param fieldValue the transformed value
     * @param field the field as created when scanning
     * @return the return value
     */
    abstract Object afterRecursiveTransform(Object fieldValue, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException;

    /**
     * This do the transform of a 'leaf' value
     *
     * @param argumentValue the value
     * @param field the field as scanned
     * @return transformed value
     */
    abstract Object singleTransform(Object argumentValue, Field field) throws AbstractDataFetcherException;

    /**
     * This does the adapting to a scalar of a 'leaf' value
     *
     * @param argumentValue the value
     * @param field the field as scanned
     * @return mapped value
     */
    abstract Object singleAdapting(Object argumentValue, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException;

    /**
     * Here we actually do the transform. This method get called recursively in the case of arrays
     *
     * @param inputValue the value we got from graphql-java or response from the method call
     * @param field details about the expected type created while scanning the code
     * @return the argumentValue in the correct type and transformed
     */
    Object recursiveTransform(Object inputValue, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {

        if (inputValue == null) {
            return null;
        }

        // First handle the array if this is an array
        if (field.hasWrapper() && field.getWrapper().isArray()) {
            return recursiveTransformArray(inputValue, field, dfe);
        } else if (field.hasWrapper() && field.getWrapper().isMap()) {
            return inputValue;
        } else if (field.hasWrapper() && field.getWrapper().isCollection()) {
            return recursiveTransformCollection(inputValue, field, dfe);
        } else if (field.hasWrapper() && field.getWrapper().isOptional()) {
            // Also handle optionals
            return recursiveTransformOptional(inputValue, field, dfe);
        } else {
            // we need to transform before we make sure the type is correct
            inputValue = singleTransform(inputValue, field);
        }

        // Recursive transformation done.
        return afterRecursiveTransform(inputValue, field, dfe);
    }

    /**
     * Here we actually do the adapting. This method get called recursively in the case of arrays etc.
     *
     * @param inputValue the value we got from graphql-java or response from the method call
     * @param field details about the expected type created while scanning the code
     * @return the argumentValue in the correct type and mapped
     */
    Object recursiveAdapting(Object inputValue, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {

        if (inputValue == null) {
            return null;
        }

        if (field.hasWrapper() && field.getWrapper().isArray()) {
            return recursiveAdaptArray(inputValue, field, dfe);
        } else if (Classes.isMap(inputValue) && shouldAdaptWithToMap(field)) {
            return singleAdapting(inputValue, field, dfe);
        } else if (shouldAdaptWithFromMap(field)) {
            return singleAdapting(new HashSet((Collection) inputValue), field, dfe);
        } else if (field.hasWrapper() && field.getWrapper().isCollection()) {
            return recursiveAdaptCollection(inputValue, field, dfe);
        } else if (field.hasWrapper() && field.getWrapper().isOptional()) {
            return recursiveAdaptOptional(inputValue, field, dfe);
        } else {
            // we need to adapt before we make sure the type is correct
            inputValue = singleAdapting(inputValue, field, dfe);
        }

        // Recursive adapting done.
        return inputValue;
    }

    /**
     * This just creates a new array and add values to it by calling the recursiveTransform method.
     * This allows arrays of arrays and transformation inside arrays
     * Even without transformation, we need to go from arrayList to Array,
     * or arraylist of arraylist to array of array
     *
     * @param array the array or list as from graphql-java,
     * @param field the field as created while scanning
     * @return an array with the transformed values in.
     */
    private Object recursiveTransformArray(Object array, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {
        if (Classes.isCollection(array)) {
            array = ((Collection) array).toArray();
        }

        Class classInCollection = getArrayType(field);

        //Skip transform if not needed
        if (array.getClass().getComponentType().equals(classInCollection)) {
            return array;
        }

        int length = Array.getLength(array);
        Object targetArray = Array.newInstance(classInCollection, length);

        for (int i = 0; i < length; i++) {
            Field fieldInCollection = getFieldInField(field);
            Object element = Array.get(array, i);
            Object targetElement = recursiveTransform(element, fieldInCollection, dfe);
            Array.set(targetArray, i, targetElement);
        }

        return targetArray;
    }

    /**
     * This just creates a new array and add values to it by calling the recursiveAdapting method.
     * This allows arrays of arrays and mapping inside arrays
     *
     * @param array the array or list as from graphql-java,
     * @param field the field as created while scanning
     * @return an array with the mapped values in.
     */
    private Object recursiveAdaptArray(Object array, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {
        if (Classes.isCollection(array)) {
            array = ((Collection) array).toArray();
        }

        Class classInCollection = getArrayType(field);

        //Skip mapping if not needed
        if (array.getClass().getComponentType().equals(classInCollection)) {
            return array;
        }

        int length = Array.getLength(array);
        Object targetArray = Array.newInstance(classInCollection, length);

        for (int i = 0; i < length; i++) {
            Field fieldInCollection = getFieldInField(field);
            Object element = Array.get(array, i);
            Object targetElement = recursiveAdapting(element, fieldInCollection, dfe);
            Array.set(targetArray, i, targetElement);
        }

        return targetArray;
    }

    /**
     * This just creates a new correct type collection and add values to it by calling the recursiveTransform method.
     * This allows collections of collections and transformation inside collections
     * Even without transformation, we need to go from arrayList to the correct collection type,
     * or arraylist of arraylist to collection of collection
     *
     * @param argumentValue the list as from graphql-java (always an arraylist)
     * @param field the field as created while scanning
     * @return a collection with the transformed values in.
     */
    private Object recursiveTransformCollection(Object argumentValue, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {
        Collection givenCollection = getGivenCollection(argumentValue);

        String collectionClassName = field.getWrapper().getWrapperClassName();

        Collection convertedCollection = CollectionCreator.newCollection(collectionClassName, givenCollection.size());

        Field fieldInCollection = getFieldInField(field);
        for (Object objectInGivenCollection : givenCollection) {
            Object objectInCollection = recursiveTransform(objectInGivenCollection,
                    fieldInCollection, dfe);
            convertedCollection.add(objectInCollection);
        }

        return convertedCollection;
    }

    /**
     * This just creates a new correct type collection and add values to it by calling the recursiveAdapting method.
     * This allows collections of collections and mapping inside collections
     *
     * @param argumentValue the list as from graphql-java (always an arraylist)
     * @param field the field as created while scanning
     * @return a collection with the mapped values in.
     */
    private Object recursiveAdaptCollection(Object argumentValue, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {
        Collection givenCollection = getGivenCollection(argumentValue);
        String collectionClassName = field.getWrapper().getWrapperClassName();
        Collection convertedCollection = CollectionCreator.newCollection(collectionClassName, givenCollection.size());

        Field fieldInCollection = getFieldInField(field);
        for (Object objectInGivenCollection : givenCollection) {
            Object objectInCollection = recursiveAdapting(objectInGivenCollection,
                    fieldInCollection, dfe);
            convertedCollection.add(objectInCollection);
        }

        return convertedCollection;
    }

    /**
     * This is not yet specified by MicroProfile GraphQL, but we support it by also allowing transformation the optional
     * element.
     *
     * @param argumentValue the value as from graphql-java
     * @param field the graphql-field
     * @return a optional with the transformed value in.
     */
    private Optional<Object> recursiveTransformOptional(Object argumentValue, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {
        // Check the type and maybe apply transformation
        if (argumentValue == null || !((Optional) argumentValue).isPresent()) {
            return Optional.empty();
        } else {
            Optional optional = (Optional) argumentValue;
            Object o = optional.get();
            Field f = getFieldInField(field);
            return Optional.of(recursiveTransform(o, f, dfe));
        }
    }

    /**
     * Add support for mapping an optional element.
     *
     * @param argumentValue the value as from graphql-java
     * @param field the graphql-field
     * @return a optional with the mapped value in.
     */
    private Optional<Object> recursiveAdaptOptional(Object argumentValue, Field field, DataFetchingEnvironment dfe)
            throws AbstractDataFetcherException {
        // Check the type and maybe apply transformation
        if (argumentValue == null || !((Optional) argumentValue).isPresent()) {
            return Optional.empty();
        } else {
            Optional optional = (Optional) argumentValue;
            Object o = optional.get();
            Field f = getFieldInField(field);
            return Optional.of(recursiveAdapting(o, f, dfe));
        }

    }

    protected Class<?> getArrayType(Field field) {
        String classNameInCollection = field.getReference().getClassName();
        Class classInCollection = classloadingService.loadClass(classNameInCollection);
        return classInCollection;
    }

    protected Transformer getTransformer(Field field) {
        if (transformerMap.containsKey(field.getName())) {
            return transformerMap.get(field.getName());
        }
        Transformer transformer = Transformer.transformer(field);
        transformerMap.put(field.getName(), transformer);
        return transformer;
    }

    protected ReflectionInvoker getReflectionInvokerForInput(AdaptWith adaptWith) {
        List<String> parameters = new ArrayList<>();
        if (adaptWith.getToReference().hasWrapper()) {
            parameters.add(adaptWith.getToReference().getWrapper().getWrapperClassName());
        } else {
            parameters.add(adaptWith.getToReference().getClassName());
        }

        return getReflectionInvoker(adaptWith.getAdapterClass(), adaptWith.getFromMethod(), parameters);

    }

    protected ReflectionInvoker getReflectionInvokerForOutput(AdaptWith adaptWith) {
        List<String> parameters = new ArrayList<>();

        if (adaptWith.getFromReference().hasWrapper()) {
            parameters.add(adaptWith.getFromReference().getWrapper().getWrapperClassName());
        } else {
            parameters.add(adaptWith.getFromReference().getClassName());
        }

        return getReflectionInvoker(adaptWith.getAdapterClass(), adaptWith.getToMethod(), parameters);
    }

    private ReflectionInvoker getReflectionInvoker(String className, String methodName, List<String> parameterClasses) {
        Integer key = getKey(className, methodName, parameterClasses);
        if (invokerMap.containsKey(key)) {
            return invokerMap.get(key);
        } else {
            ReflectionInvoker reflectionInvoker = new ReflectionInvoker(className, methodName, parameterClasses);
            invokerMap.put(key, reflectionInvoker);
            return reflectionInvoker;
        }
    }

    private Integer getKey(String className, String methodName, List<String> parameterClasses) {
        return Objects.hash(className, methodName, Arrays.hashCode(parameterClasses.toArray()));
    }

    /**
     * Here we create an field from a field, but reducing the collection depth.
     * This will happen until we get to the field in the collection.
     * <p>
     * This 'clone' an array, but update the array information
     *
     * @param owner the owner field
     * @return the new field
     */
    private Field getFieldInField(Field owner) {
        Field child = new Field(owner.getMethodName(),
                owner.getPropertyName(),
                owner.getName(),
                owner.getReference());
        // not null
        child.setNotNull(owner.isNotNull());
        // description
        child.setDescription(owner.getDescription());
        // transform info
        child.setTransformation(owner.getTransformation());
        // adapting to
        child.setAdaptTo(owner.getAdaptTo());
        // adapting with
        child.setAdaptWith(owner.getAdaptWith());
        // default value
        child.setDefaultValue(owner.getDefaultValue());

        // wrapper
        Wrapper wrapper = owner.getWrapper();
        if (owner.hasWrapper()) {
            Wrapper ownerWrapper = owner.getWrapper();
            if (ownerWrapper.getWrapper() != null) {
                // We still not at the end
                Wrapper childWrapper = ownerWrapper.getWrapper();
                child.setWrapper(childWrapper);
            }
        }
        return child;

    }

    private <T> Collection getGivenCollection(Object argumentValue) {
        if (Classes.isCollection(argumentValue)) {
            return (Collection) argumentValue;
        } else {
            return Arrays.asList((T[]) argumentValue);
        }
    }

    // TODO: Support more concrete maps too
    private boolean shouldAdaptWithFromMap(Field field) {
        if (field.isAdaptingWith()) {
            return field.getAdaptWith().getFromReference().getClassName().equals(Map.class.getName())
                    || (field.getAdaptWith().getFromReference().hasWrapper()
                            && field.getAdaptWith().getFromReference().getWrapper().getWrapperClassName()
                                    .equals(Map.class.getName()));
        }

        return field.hasWrapper() && field.getWrapper().isMap();
    }

    private boolean shouldAdaptWithToMap(Field field) {
        if (field.isAdaptingWith()) {
            return field.getAdaptWith().getToReference().hasWrapper()
                    && field.getAdaptWith().getToReference().getWrapper().getWrapperClassName().equals(Set.class.getName())
                    && field.getAdaptWith().getToReference().getClassName().equals(Entry.class.getName());
        }

        return field.hasWrapper() && field.getWrapper().isMap();
    }

}

package io.smallrye.graphql.execution.datafetcher.helper;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.execution.datafetcher.CollectionCreator;
import io.smallrye.graphql.schema.model.Adapter;
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
    private final Map<String, Transformer> transformerMap = new HashMap<>();
    private final Map<Integer, ReflectionInvoker> invokerMap = new HashMap<>();

    protected AbstractHelper() {
    }

    /**
     * This gets called after the value has been recursively transformed.
     *
     * @param fieldValue the transformed value
     * @param field the field as created when scanning
     * @return the return value
     */
    abstract Object afterRecursiveTransform(Object fieldValue, Field field) throws AbstractDataFetcherException;

    /**
     * This do the transform of a 'leaf' value
     *
     * @param argumentValue the value
     * @param field the field as scanned
     * @return transformed value
     */
    abstract Object singleTransform(Object argumentValue, Field field) throws AbstractDataFetcherException;

    /**
     * This does the mapping of a 'leaf' value
     *
     * @param argumentValue the value
     * @param field the field as scanned
     * @return mapped value
     */
    abstract Object singleMapping(Object argumentValue, Field field) throws AbstractDataFetcherException;

    /**
     * Here we actually do the transform. This method get called recursively in the case of arrays
     *
     * @param inputValue the value we got from graphql-java or response from the method call
     * @param field details about the expected type created while scanning the code
     * @return the argumentValue in the correct type and transformed
     */
    Object recursiveTransform(Object inputValue, Field field)
            throws AbstractDataFetcherException {

        if (inputValue == null) {
            return null;
        }

        // First handle the array if this is an array
        if (field.hasWrapper() && field.getWrapper().isArray()) {
            return recursiveTransformArray(inputValue, field);
        } else if (field.hasWrapper() && field.getWrapper().isCollection()) {
            return recursiveTransformCollection(inputValue, field);
        } else if (field.hasWrapper() && field.getWrapper().isOptional()) {
            // Also handle optionals
            return recursiveTransformOptional(inputValue, field);
        } else {
            // we need to transform before we make sure the type is correct
            inputValue = singleTransform(inputValue, field);
        }

        // Recursive transformation done.
        return afterRecursiveTransform(inputValue, field);
    }

    /**
     * Here we actually do the mapping. This method get called recursively in the case of arrays
     *
     * @param inputValue the value we got from graphql-java or response from the method call
     * @param field details about the expected type created while scanning the code
     * @return the argumentValue in the correct type and mapped
     */
    Object recursiveMapping(Object inputValue, Field field)
            throws AbstractDataFetcherException {

        if (inputValue == null) {
            return null;
        }

        if (field.hasWrapper() && field.getWrapper().isArray()) {
            return recursiveMappingArray(inputValue, field);
        } else if (field.hasWrapper() && field.getWrapper().isCollection()) {
            return recursiveMappingCollection(inputValue, field);
        } else if (field.hasWrapper() && field.getWrapper().isOptional()) {
            return recursiveMappingOptional(inputValue, field);
        } else {
            // we need to transform before we make sure the type is correct
            inputValue = singleMapping(inputValue, field);
        }

        // Recursive transformation done.
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
    private Object recursiveTransformArray(Object array, Field field) throws AbstractDataFetcherException {
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
            Object targetElement = recursiveTransform(element, fieldInCollection);
            Array.set(targetArray, i, targetElement);
        }

        return targetArray;
    }

    /**
     * This just creates a new array and add values to it by calling the recursiveMapping method.
     * This allows arrays of arrays and mapping inside arrays
     *
     * @param array the array or list as from graphql-java,
     * @param field the field as created while scanning
     * @return an array with the mapped values in.
     */
    private Object recursiveMappingArray(Object array, Field field) throws AbstractDataFetcherException {
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
            Object targetElement = recursiveMapping(element, fieldInCollection);
            Array.set(targetArray, i, targetElement);
        }

        return targetArray;
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

    protected ReflectionInvoker getReflectionInvoker(Adapter adapter) {
        List<String> parameters = new ArrayList<>();
        parameters.add(adapter.getFromClass());
        return getReflectionInvoker(adapter.getAdapterClass(), adapter.getToMethod(), parameters);
    }

    protected ReflectionInvoker getReflectionInvoker(String className, String methodName, List<String> parameterClasses) {
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
        return Objects.hash(className, methodName, parameterClasses.toArray());
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
                && !field.getReference().getClassName().equals(field.getReference().getGraphQlClassName())
                || field.hasAdapter());
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
    private Object recursiveTransformCollection(Object argumentValue, Field field) throws AbstractDataFetcherException {
        Collection givenCollection = getGivenCollection(argumentValue);

        String collectionClassName = field.getWrapper().getWrapperClassName();

        Collection convertedCollection = CollectionCreator.newCollection(collectionClassName, givenCollection.size());

        for (Object objectInGivenCollection : givenCollection) {
            Field fieldInCollection = getFieldInField(field);
            Object objectInCollection = recursiveTransform(objectInGivenCollection,
                    fieldInCollection);
            convertedCollection.add(objectInCollection);
        }

        return convertedCollection;
    }

    /**
     * This just creates a new correct type collection and add values to it by calling the recursiveMapping method.
     * This allows collections of collections and mapping inside collections
     *
     * @param argumentValue the list as from graphql-java (always an arraylist)
     * @param field the field as created while scanning
     * @return a collection with the mapped values in.
     */
    private Object recursiveMappingCollection(Object argumentValue, Field field) throws AbstractDataFetcherException {
        Collection givenCollection = getGivenCollection(argumentValue);
        String collectionClassName = field.getWrapper().getWrapperClassName();
        Collection convertedCollection = CollectionCreator.newCollection(collectionClassName, givenCollection.size());

        for (Object objectInGivenCollection : givenCollection) {
            Field fieldInCollection = getFieldInField(field);
            Object objectInCollection = recursiveMapping(objectInGivenCollection,
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
     * @param field the graphql-field
     * @return a optional with the transformed value in.
     */
    private Optional<Object> recursiveTransformOptional(Object argumentValue, Field field) throws AbstractDataFetcherException {
        // Check the type and maybe apply transformation
        if (argumentValue == null || !((Optional) argumentValue).isPresent()) {
            return Optional.empty();
        } else {
            Optional optional = (Optional) argumentValue;
            Object o = optional.get();
            Field f = getFieldInField(field);
            return Optional.of(recursiveTransform(o, f));
        }

    }

    /**
     * Add support for mapping an optional element.
     *
     * @param argumentValue the value as from graphql-java
     * @param field the graphql-field
     * @return a optional with the mapped value in.
     */
    private Optional<Object> recursiveMappingOptional(Object argumentValue, Field field) throws AbstractDataFetcherException {
        // Check the type and maybe apply transformation
        if (argumentValue == null || !((Optional) argumentValue).isPresent()) {
            return Optional.empty();
        } else {
            Optional optional = (Optional) argumentValue;
            Object o = optional.get();
            Field f = getFieldInField(field);
            return Optional.of(recursiveMapping(o, f));
        }

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
        // mapping info
        child.setMapping(owner.getMapping());
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

}

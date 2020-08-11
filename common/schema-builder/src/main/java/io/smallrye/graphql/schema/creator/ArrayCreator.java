package io.smallrye.graphql.schema.creator;

import java.util.Optional;

import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.model.Array;

/**
 * Helper with detecting if this is an array Field.
 * If it is we create an Array model that contains the relevant information
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class ArrayCreator {

    private ArrayCreator() {
    }

    public static Optional<Array> createArray(Type type) {
        return createArray(null, type, false);
    }

    /**
     * Create an Array for a Type.
     * This is used by operations, arguments and interfaces, as there is no field type
     * 
     * @param type the java method/argument type
     * @return optional array
     */
    public static Optional<Array> createArray(Type type, boolean batched) {
        return createArray(null, type, batched);
    }

    /**
     * Create a array for a Field (that has properties and methods)
     * 
     * @param fieldType the java field type
     * @param methodType the java method type
     * @return optional array
     */
    public static Optional<Array> createArray(Type fieldType, Type methodType) {
        return createArray(fieldType, methodType, false);
    }

    public static Optional<Array> createArray(Type fieldType, Type methodType, boolean batched) {
        if (Classes.isCollectionOrArray(methodType) && !batched) {
            Array.Type arrayType = getModelType(methodType);
            int depth = getParameterizedDepth(methodType);
            Array array = new Array(methodType.name().toString(), arrayType, depth);
            // NotNull
            if (markParameterizedTypeNonNull(fieldType, methodType)) {
                array.setNotEmpty(true);
            }
            return Optional.of(array);
        } else if (Classes.isUnwrappedType(methodType) || batched) {
            Type nestedType = methodType.asParameterizedType().arguments().get(0);
            return createArray(nestedType);
        }
        return Optional.empty();
    }

    private static Array.Type getModelType(Type type) {
        if (type.kind().equals(Type.Kind.ARRAY)) {
            return Array.Type.ARRAY;
        }
        return Array.Type.COLLECTION;
    }

    private static int getParameterizedDepth(Type type) {
        return getParameterizedDepth(type, 0);
    }

    private static int getParameterizedDepth(Type type, int depth) {
        if (type.kind().equals(Type.Kind.ARRAY)) {
            depth = depth + 1;
            Type typeInArray = type.asArrayType().component();
            return getParameterizedDepth(typeInArray, depth);
        } else if (Classes.isCollection(type)) {
            depth = depth + 1;
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return getParameterizedDepth(typeInCollection, depth);
        } else if (Classes.isUnwrappedType(type)) {
            Type nestedType = type.asParameterizedType().arguments().get(0);
            return getParameterizedDepth(nestedType, depth);
        }
        return depth;
    }

    private static boolean markParameterizedTypeNonNull(Type fieldType, Type methodType) {
        if (fieldType == null)
            fieldType = methodType;
        if (Classes.isCollectionOrArray(fieldType)) {
            Type typeInCollection = getTypeInCollection(fieldType);
            Type methodTypeInCollection = getTypeInCollection(methodType);
            Annotations annotationsInParameterizedType = Annotations.getAnnotationsForArray(typeInCollection,
                    methodTypeInCollection);

            return NonNullHelper.markAsNonNull(typeInCollection, annotationsInParameterizedType, true);
        }
        return false;
    }

    private static Type getTypeInCollection(Type type) {
        if (Classes.isCollectionOrArray(type)) {
            if (type.kind().equals(Type.Kind.ARRAY)) {
                Type typeInArray = type.asArrayType().component();
                return getTypeInCollection(typeInArray);
            } else {
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return getTypeInCollection(typeInCollection);
            }
        }
        return type;

    }
}

package io.smallrye.graphql.schema.creator;

import java.util.Optional;

import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.helper.NonNullHelper;
import io.smallrye.graphql.schema.model.Wrapper;
import io.smallrye.graphql.schema.model.WrapperType;

/**
 * Helper with detecting if this field is in a wrapper
 * 
 * If it is we create an WrapperInfo model that contains the relevant information
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class WrapperCreator {

    private WrapperCreator() {
    }

    public static Optional<Wrapper> createWrapper(Type type) {
        return createWrapper(null, type);
    }

    /**
     * Create a Wrapper for a Field (that has properties and methods)
     * 
     * @param fieldType the java field type
     * @param methodType the java method type
     * @return optional array
     */
    public static Optional<Wrapper> createWrapper(Type fieldType, Type methodType) {
        if (Classes.isWrapper(methodType)) {
            Wrapper wrapper = new Wrapper(getWrapperType(methodType), methodType.name().toString());
            // NotNull
            if (markParameterizedTypeNonNull(fieldType, methodType)) {
                wrapper.setNotEmpty(true);
            }
            // Wrapper of wrapper
            Optional<Wrapper> wrapperOfWrapper = getWrapperOfWrapper(methodType);
            if (wrapperOfWrapper.isPresent()) {
                wrapper.setWrapper(wrapperOfWrapper.get());
            }

            return Optional.of(wrapper);
        }
        return Optional.empty();
    }

    private static WrapperType getWrapperType(Type type) {
        if (Classes.isOptional(type)) {
            return WrapperType.OPTIONAL;
        } else if (Classes.isArray(type)) {
            return WrapperType.ARRAY;
        } else if (Classes.isCollection(type)) {
            return WrapperType.COLLECTION;
        } else if (Classes.isMap(type)) {
            return WrapperType.MAP;
        } else if (Classes.isParameterized(type)) {
            return WrapperType.UNKNOWN;
        }
        return null;
    }

    private static Optional<Wrapper> getWrapperOfWrapper(Type type) {
        if (Classes.isArray(type)) {
            Type typeInArray = type.asArrayType().component();
            return createWrapper(typeInArray);
        } else if (Classes.isParameterized(type)) {
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return createWrapper(typeInCollection);
        }
        return Optional.empty();
    }

    private static boolean markParameterizedTypeNonNull(Type fieldType, Type methodType) {
        if (fieldType == null)
            fieldType = methodType;
        if (Classes.isWrapper(fieldType)) {
            Type typeInCollection = getTypeInCollection(fieldType);
            Type methodTypeInCollection = getTypeInCollection(methodType);
            Annotations annotationsInParameterizedType = Annotations.getAnnotationsForArray(typeInCollection,
                    methodTypeInCollection);

            return NonNullHelper.markAsNonNull(typeInCollection, annotationsInParameterizedType, true);
        }
        return false;
    }

    private static Type getTypeInCollection(Type type) {
        if (Classes.isArray(type)) {
            Type typeInArray = type.asArrayType().component();
            return getTypeInCollection(typeInArray);
        } else if (Classes.isParameterized(type)) {
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return getTypeInCollection(typeInCollection);
        }
        return type;
    }
}

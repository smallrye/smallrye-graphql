package io.smallrye.graphql.schema.helper;

import java.util.Map;
import java.util.Optional;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import io.smallrye.graphql.schema.Annotations;
import io.smallrye.graphql.schema.Classes;
import io.smallrye.graphql.schema.CreationException;
import io.smallrye.graphql.schema.ObjectBag;
import io.smallrye.graphql.schema.Scalars;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;

/**
 * Shared code between model creators and schema creator.
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CreatorHelper {
    private static final Logger LOG = Logger.getLogger(CreatorHelper.class.getName());

    private CreatorHelper() {
    }

    public static boolean isParameterized(Type type) {
        return type.kind().equals(Type.Kind.ARRAY) || type.kind().equals(Type.Kind.PARAMETERIZED_TYPE);
    }

    public static Field getReturnField(IndexView index, ReferenceType referenceType, Type methodType,
            Annotations annotations) {
        Reference returnTypeRef = getReference(index, referenceType, methodType,
                annotations);
        return createReturnField(returnTypeRef, methodType, methodType, annotations);
    }

    public static Field getReturnField(IndexView index, ReferenceType referenceType, Type fieldType, Type methodType,
            Annotations annotations) {

        Reference returnTypeRef = getReference(index, referenceType, fieldType,
                methodType, annotations);

        return createReturnField(returnTypeRef, fieldType, methodType, annotations);
    }

    public static Reference getReference(IndexView index, ReferenceType referenceType, Type methodType,
            Annotations annotations) {
        return getReference(index, referenceType, null, methodType, annotations);
    }

    public static Reference getReference(IndexView index, ReferenceType referenceType, Type fieldType, Type methodType,
            Annotations annotations) {

        if (fieldType == null)
            fieldType = methodType;

        String fieldTypeName = fieldType.name().toString();

        if (annotations.containsOneOfTheseKeys(Annotations.ID)) {
            // ID
            return Scalars.getIDScalar();
        } else if (Scalars.isScalar(fieldTypeName)) {
            // Scalar
            return Scalars.getScalar(fieldTypeName);
        } else if (fieldType.kind().equals(Type.Kind.ARRAY)) {
            // Array 
            Type typeInArray = fieldType.asArrayType().component();
            Type typeInMethodArray = methodType.asArrayType().component();
            return getReference(index, referenceType, typeInArray, typeInMethodArray, annotations);
        } else if (fieldType.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            Type typeInCollection = fieldType.asParameterizedType().arguments().get(0);
            Type typeInMethodCollection = methodType.asParameterizedType().arguments().get(0);
            return getReference(index, referenceType, typeInCollection, typeInMethodCollection, annotations);
        } else if (fieldType.kind().equals(Type.Kind.CLASS)) {
            ClassInfo classInfo = index.getClassByName(fieldType.name());
            if (classInfo != null) {
                Annotations annotationsForThisClass = AnnotationsHelper.getAnnotationsForClass(classInfo);
                if (Classes.isEnum(classInfo)) {
                    String name = NameHelper.getAnyTypeName(ReferenceType.ENUM, classInfo, annotationsForThisClass);
                    return toBeScanned(ReferenceType.ENUM, classInfo, name);
                } else {
                    String name = NameHelper.getAnyTypeName(referenceType, classInfo, annotationsForThisClass);
                    return toBeScanned(referenceType, classInfo, name);
                }
            } else {
                LOG.warn("Class [" + fieldType.name()
                        + "] in not indexed in Jandex. Can not create Type, defaulting to String Scalar");
                return Scalars.getScalar(String.class.getName()); // default
            }
        } else {
            throw new CreationException("Don't know what to do with [" + fieldType + "] of kind [" + fieldType.kind() + "]");
        }
    }

    public static Field getParameter(IndexView index, ReferenceType referenceType, Type type, MethodInfo methodInfo,
            short position) {

        // Type
        Type methodParameter = type;
        if (methodInfo.parameters() != null && !methodInfo.parameters().isEmpty()) {
            methodParameter = methodInfo.parameters().get(position);
        }

        Annotations annotationsForThisArgument = AnnotationsHelper.getAnnotationsForArgument(methodInfo, position);
        Reference parameterRef = getReference(index, referenceType, type,
                methodParameter, annotationsForThisArgument);

        // Name
        String defaultName = methodInfo.parameterName(position);
        String name = NameHelper.getArgumentName(annotationsForThisArgument, defaultName);

        // Description    
        Optional<String> description = DescriptionHelper.getDescriptionForField(annotationsForThisArgument, type);

        Field parameter = new Field(name, description.orElse(null), parameterRef);

        // Default Value
        Optional<Object> defaultValue = DefaultValueHelper.getDefaultValue(annotationsForThisArgument);
        parameter.setDefaultValue(defaultValue.orElse(null));

        // Collection
        if (isParameterized(type)) {
            int depth = getParameterizedDepth(type);
            parameter.setCollectionDepth(depth);
        }

        // NotNull
        if (NonNullHelper.markAsNonNull(type, annotationsForThisArgument)) {
            parameter.setMandatory(true);
        }
        parameter.setMandatoryInCollection(markParameterizedTypeNonNull(type, methodParameter));

        return parameter;
    }

    // Add to the correct map to be scanned later.
    public static Reference toBeScanned(ReferenceType referenceType, ClassInfo classInfo, String name) {
        String className = classInfo.name().toString();
        // First check if this is an interface
        if (Classes.isInterface(classInfo)) {
            referenceType = ReferenceType.INTERFACE;
        }

        Reference reference = new Reference(className, name, referenceType);
        Map<String, Reference> map = ObjectBag.getReferenceMap(referenceType);
        if (!map.containsKey(className)) {
            map.put(className, reference);
        }
        return reference;
    }

    private static Field createReturnField(Reference returnTypeRef, Type fieldType, Type methodType, Annotations annotations) {
        // Name
        String name = NameHelper.getAnyNameForField(Direction.OUT, annotations, methodType.name().toString());
        // Description
        Optional<String> maybeFieldDescription = DescriptionHelper.getDescriptionForField(annotations, methodType);

        Field returnField = new Field(name, maybeFieldDescription.orElse(null), returnTypeRef);

        // Collection
        if (isParameterized(methodType)) {
            int depth = getParameterizedDepth(methodType);
            returnField.setCollectionDepth(depth);
        }

        // NotNull
        if (NonNullHelper.markAsNonNull(methodType, annotations)) {
            returnField.setMandatory(true);
        }
        returnField.setMandatoryInCollection(markParameterizedTypeNonNull(fieldType, methodType));

        // Default value (on method)
        Optional<Object> maybeDefaultValue = DefaultValueHelper.getDefaultValue(annotations);
        returnField.setDefaultValue(maybeDefaultValue.orElse(null));

        return returnField;
    }

    private static int getParameterizedDepth(Type type) {
        return getParameterizedDepth(type, 0);
    }

    private static int getParameterizedDepth(Type type, int depth) {
        if (type.kind().equals(Type.Kind.ARRAY)) {
            depth = depth + 1;
            Type typeInArray = type.asArrayType().component();
            return getParameterizedDepth(typeInArray, depth);
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            depth = depth + 1;
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return getParameterizedDepth(typeInCollection, depth);
        }
        return depth;
    }

    private static Type getTypeInCollection(Type type) {
        if (isParameterized(type)) {
            if (type.kind().equals(Type.Kind.ARRAY)) {
                Type typeInArray = type.asArrayType().component();
                return getTypeInCollection(typeInArray);
            } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return getTypeInCollection(typeInCollection);
            }
        }
        return type;

    }

    public static boolean markParameterizedTypeNonNull(Type type, Type methodType) {
        if (type == null)
            type = methodType;
        if (isParameterized(type)) {
            Type typeInCollection = getTypeInCollection(type);
            Type methodTypeInCollection = getTypeInCollection(methodType);
            Annotations annotationsInParameterizedType = AnnotationsHelper.getAnnotationsForType(typeInCollection,
                    methodTypeInCollection);

            return NonNullHelper.markAsNonNull(typeInCollection, annotationsInParameterizedType, true);
        }
        return false;

    }

}

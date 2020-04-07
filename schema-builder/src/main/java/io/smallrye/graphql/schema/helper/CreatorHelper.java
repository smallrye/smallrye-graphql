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
import io.smallrye.graphql.schema.model.Parameter;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Return;

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

    public static Return getReturnField(IndexView index, ReferenceType referenceType, Type methodType,
            Annotations annotations) {
        Reference returnTypeRef = getReference(index, referenceType, methodType,
                annotations);
        return getReturnField(returnTypeRef, methodType, annotations);
    }

    public static Return getReturnField(IndexView index, ReferenceType referenceType, Type fieldType, Type methodType,
            Annotations annotations) {

        Reference returnTypeRef = getReference(index, referenceType, fieldType,
                methodType, annotations);

        return getReturnField(returnTypeRef, methodType, annotations);
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

    public static Parameter getParameter(IndexView index, ReferenceType referenceType, Type type, MethodInfo methodInfo,
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

        Parameter parameter = new Parameter(name, description.orElse(null), parameterRef);

        // Default Value
        Optional<Object> defaultValue = DefaultValueHelper.getDefaultValue(annotationsForThisArgument);
        parameter.setDefaultValue(defaultValue.orElse(null));

        // Collection
        if (CreatorHelper.isParameterized(type)) {
            parameter.setCollection(true);
        }

        // NotNull
        if (NonNullHelper.markAsNonNull(type, annotationsForThisArgument)) {
            parameter.setMandatory(true);
        }

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

    private static Return getReturnField(Reference returnTypeRef, Type methodType, Annotations annotations) {
        // Name
        String name = NameHelper.getAnyNameForField(Direction.OUT, annotations, methodType.name().toString());
        // Description
        Optional<String> maybeFieldDescription = DescriptionHelper.getDescriptionForField(annotations, methodType);

        Return returnField = new Return(name, maybeFieldDescription.orElse(null), returnTypeRef);

        // Collection
        if (CreatorHelper.isParameterized(methodType)) {
            returnField.setCollection(true);
        }

        // NotNull
        if (NonNullHelper.markAsNonNull(methodType, annotations)) {
            returnField.setMandatory(true);
        }
        return returnField;
    }

}

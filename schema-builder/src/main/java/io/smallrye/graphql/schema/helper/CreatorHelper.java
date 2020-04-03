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
import io.smallrye.graphql.schema.model.DefinitionType;
import io.smallrye.graphql.schema.model.Parameter;
import io.smallrye.graphql.schema.model.Reference;

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

    public static Reference getReference(IndexView index, DefinitionType definitionType, Type methodType,
            Annotations annotations) {
        return getReference(index, definitionType, null, methodType, annotations);
    }

    public static Reference getReference(IndexView index, DefinitionType definitionType, Type fieldType, Type methodType,
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
            return getReference(index, definitionType, typeInArray, typeInMethodArray, annotations);
        } else if (fieldType.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            Type typeInCollection = fieldType.asParameterizedType().arguments().get(0);
            Type typeInMethodCollection = methodType.asParameterizedType().arguments().get(0);
            return getReference(index, definitionType, typeInCollection, typeInMethodCollection, annotations);
        } else if (fieldType.kind().equals(Type.Kind.CLASS)) {
            ClassInfo classInfo = index.getClassByName(fieldType.name());
            if (classInfo != null) {
                Annotations annotationsForThisClass = AnnotationsHelper.getAnnotationsForClass(classInfo);
                if (Classes.isEnum(classInfo)) {
                    String name = NameHelper.getAnyTypeName(DefinitionType.ENUM, classInfo, annotationsForThisClass);
                    return toBeScanned(DefinitionType.ENUM, classInfo, name);
                } else {
                    String name = NameHelper.getAnyTypeName(definitionType, classInfo, annotationsForThisClass);
                    return toBeScanned(definitionType, classInfo, name);
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

    public static Parameter getParameter(IndexView index, Type type, MethodInfo methodInfo, short position) {
        Parameter parameter = new Parameter();

        // Type
        Type methodParameter = type;
        if (methodInfo.parameters() != null && !methodInfo.parameters().isEmpty()) {
            methodParameter = methodInfo.parameters().get(position);
        }
        Annotations annotationsForThisArgument = AnnotationsHelper.getAnnotationsForArgument(methodInfo, position);
        Reference parameterRef = getReference(index, DefinitionType.INPUT, type,
                methodParameter, annotationsForThisArgument);
        parameter.setParameterType(parameterRef);
        if (CreatorHelper.isParameterized(type)) {
            parameter.setCollection(true);
        }

        // Name
        String defaultName = methodInfo.parameterName(position);
        String argName = NameHelper.getArgumentName(annotationsForThisArgument, defaultName);
        parameter.setName(argName);

        // Description    
        Optional<String> description = DescriptionHelper.getDescriptionForField(annotationsForThisArgument, type);
        parameter.setDescription(description.orElse(null));

        // Default Value
        Optional<Object> defaultValue = DefaultValueHelper.getDefaultValue(annotationsForThisArgument);
        parameter.setDefaultValue(defaultValue.orElse(null));

        // NotNull
        if (NonNullHelper.markAsNonNull(type, annotationsForThisArgument)) {
            parameter.setMandatory(true);
        }

        return parameter;
    }

    // Add to the correct map to be scanned later.
    public static Reference toBeScanned(DefinitionType definitionType, ClassInfo classInfo, String name) {
        String className = classInfo.name().toString();
        // First check if this is an interface
        if (Classes.isInterface(classInfo)) {
            definitionType = DefinitionType.INTERFACE;
        }

        Reference reference = new Reference(className, name, definitionType);
        Map<String, Reference> map = ObjectBag.getReferenceMap(definitionType);
        if (!map.containsKey(className)) {
            map.put(className, reference);
        }
        return reference;
    }

}

package io.smallrye.graphql.schema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;

import io.smallrye.graphql.schema.helper.Direction;

/**
 * All the annotations we care about for a certain context
 * 
 * There are multiple static methods to create the annotations for the correct context
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Annotations {

    private final Map<DotName, AnnotationInstance> annotationsMap;

    /**
     * Get used when creating operations.
     * Operation only have methods (no properties)
     * 
     * @param methodInfo the java method
     * @return Annotations for this method
     */
    public static Annotations getAnnotationsForMethod(MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : methodInfo.annotations()) {
            DotName name = annotationInstance.name();
            AnnotationTarget.Kind kind = annotationInstance.target().kind();
            if (kind.equals(AnnotationTarget.Kind.METHOD)) {
                annotationMap.put(name, annotationInstance);
            }
        }
        return new Annotations(annotationMap);
    }

    /**
     * Get used when creating fields on interfaces.
     * Interfaces only has methods, no properties
     * 
     * @param methodInfo the java method
     * @return Annotations for this method
     */
    public static Annotations getAnnotationsForInterfaceField(MethodInfo methodInfo) {
        return getAnnotationsForOutputField(null, methodInfo);
    }

    /**
     * Get used when creating fields on inputs and types.
     * Both has properties and methods and this needs to combined the two
     * 
     * @param direction the direction
     * @param fieldInfo the java property
     * @param methodInfo the java method
     * @return annotations for this field
     */
    public static Annotations getAnnotationsForPojo(Direction direction, FieldInfo fieldInfo, MethodInfo methodInfo) {
        if (direction.equals(Direction.IN)) {
            return getAnnotationsForInputField(fieldInfo, methodInfo);
        } else {
            return getAnnotationsForOutputField(fieldInfo, methodInfo);
        }
    }

    /**
     * Get used when we create types and references to them
     * 
     * Class level annotation for type creation.
     * 
     * @param classInfo the java class
     * @return annotation for this class
     */
    public static Annotations getAnnotationsForClass(ClassInfo classInfo) {

        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : classInfo.classAnnotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }

        return new Annotations(annotationMap);
    }

    /**
     * Get used when creating arrays.
     * 
     * This will contains the annotation on the collection field and method
     * 
     * @param typeInCollection the field java type
     * @param methodTypeInCollection the method java type
     * @return the annotation for this array
     */
    public static Annotations getAnnotationsForArray(org.jboss.jandex.Type typeInCollection,
            org.jboss.jandex.Type methodTypeInCollection) {
        Map<DotName, AnnotationInstance> annotationMap = getAnnotationsForType(typeInCollection);
        annotationMap.putAll(getAnnotationsForType(methodTypeInCollection));
        return new Annotations(annotationMap);
    }

    // 
    /**
     * Used when we are creating operation and arguments for these operations
     * 
     * @param methodInfo the java method
     * @param pos the argument position
     * @return annotation for this argument
     */
    public static Annotations getAnnotationsForArgument(MethodInfo methodInfo, short pos) {
        if (pos >= methodInfo.parameters().size()) {
            throw new IndexOutOfBoundsException(
                    "Parameter at position " + pos + " not found on method " + methodInfo.name());
        }

        final org.jboss.jandex.Type parameterType = methodInfo.parameters().get(pos);

        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        annotationMap.putAll(getAnnotations(parameterType));

        for (AnnotationInstance anno : methodInfo.annotations()) {
            if (anno.target().kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                MethodParameterInfo methodParameter = anno.target().asMethodParameter();
                short position = methodParameter.position();
                if (position == pos) {
                    annotationMap.put(anno.name(), anno);
                }
            }
        }

        return new Annotations(annotationMap);
    }

    // ------- All static creators done, now the actual class --------

    /**
     * Create the annotations, mapped by name
     * 
     * @param annotations the annotation
     */
    private Annotations(Map<DotName, AnnotationInstance> annotations) {
        this.annotationsMap = annotations;
    }

    public Set<DotName> getAnnotationNames() {
        return annotationsMap.keySet();
    }

    /**
     * Get a specific annotation
     * 
     * @param annotation the annotation you want
     * @return the annotation instance or null
     */
    public AnnotationValue getAnnotationValue(DotName annotation) {
        return this.annotationsMap.get(annotation).value();
    }

    /**
     * Check if there is an annotation and it has a valid value
     * 
     * @param annotation the annotation we are checking
     * @return true if valid value
     */
    public boolean containsKeyAndValidValue(DotName annotation) {
        return this.annotationsMap.containsKey(annotation) && this.annotationsMap.get(annotation).value() != null;
    }

    /**
     * Check if one of these annotations is present
     * 
     * @param annotations the annotations to check
     * @return true if it does
     */
    public boolean containsOneOfTheseAnnotations(DotName... annotations) {
        for (DotName name : annotations) {
            if (this.annotationsMap.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get on of these annotations
     * 
     * @param annotations the annotations to check (in order)
     * @return the annotation potentially or empty if not found
     */
    public Optional<AnnotationInstance> getOneOfTheseAnnotations(DotName... annotations) {
        for (DotName name : annotations) {
            if (this.annotationsMap.containsKey(name)) {
                return Optional.of(this.annotationsMap.get(name));
            }
        }
        return Optional.empty();
    }

    /**
     * This go through a list of annotations and find the first one that has a valid value.
     * If it could not find one, it return empty
     * 
     * @param annotations the annotations in order
     * @return the valid annotation value or default value
     */
    public Optional<String> getOneOfTheseAnnotationsValue(DotName... annotations) {
        for (DotName dotName : annotations) {
            if (dotName != null && containsKeyAndValidValue(dotName)) {
                return getStringValue(dotName);
            }
        }
        return Optional.empty();
    }

    /**
     * This go through a list of method annotations and find the first one that has a valid value.
     * If it could not find one, it return the default value.
     * 
     * @param annotations the annotations in order
     * @return the valid annotation value or empty
     */
    public Optional<String> getOneOfTheseMethodAnnotationsValue(DotName... annotations) {
        for (DotName dotName : annotations) {
            if (dotName != null && hasValidMethodAnnotation(dotName)) {
                return getStringValue(dotName);
            }
        }
        return Optional.empty();
    }

    /**
     * This go through a list of method parameter annotations and find the first one that has a valid value.
     * If it could not find one, it return the default value.
     * 
     * @param annotations the annotations in order
     * @return the valid annotation value or empty
     */
    public Optional<String> getOneOfTheseMethodParameterAnnotationsValue(DotName... annotations) {
        for (DotName dotName : annotations) {
            if (dotName != null && hasValidMethodParameterAnnotation(dotName)) {
                return getStringValue(dotName);
            }
        }
        return Optional.empty();
    }

    private boolean hasValidMethodAnnotation(DotName annotation) {
        if (containsKeyAndValidValue(annotation)) {
            AnnotationInstance annotationInstance = getAnnotation(annotation);
            if (isMethodAnnotation(annotationInstance)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasValidMethodParameterAnnotation(DotName annotation) {
        if (containsKeyAndValidValue(annotation)) {
            AnnotationInstance annotationInstance = getAnnotation(annotation);
            if (isMethodParameterAnnotation(annotationInstance)) {
                return true;
            }
        }
        return false;
    }

    private AnnotationInstance getAnnotation(DotName key) {
        return this.annotationsMap.get(key);
    }

    private Optional<String> getStringValue(DotName annotation) {
        AnnotationInstance annotationInstance = getAnnotation(annotation);
        if (annotationInstance != null) {
            return getStringValue(annotationInstance);
        }
        return Optional.empty();
    }

    private Optional<String> getStringValue(AnnotationInstance annotationInstance) {
        AnnotationValue value = annotationInstance.value();
        if (value != null) {
            return getStringValue(value);
        }
        return Optional.empty();
    }

    private Optional<String> getStringValue(AnnotationValue annotationValue) {
        String value = null;
        if (annotationValue != null) {
            value = annotationValue.asString();
            if (value != null && !value.isEmpty()) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    // Private static methods use by the static initializers

    private static boolean isMethodAnnotation(AnnotationInstance instance) {
        return instance.target().kind().equals(AnnotationTarget.Kind.METHOD);
    }

    private static boolean isMethodParameterAnnotation(AnnotationInstance instance) {
        return instance.target().kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER);
    }

    private static Annotations getAnnotationsForInputField(FieldInfo fieldInfo, MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationsForField = getAnnotationsForField(fieldInfo, methodInfo);

        List<org.jboss.jandex.Type> parameters = methodInfo.parameters();
        if (parameters != null && !parameters.isEmpty()) {
            org.jboss.jandex.Type param = parameters.get(ZERO);
            Map<DotName, AnnotationInstance> parameterAnnotations = getAnnotationsWithFilter(param, Annotations.DATE_FORMAT,
                    Annotations.NUMBER_FORMAT);
            if (!parameterAnnotations.isEmpty()) {
                annotationsForField.putAll(parameterAnnotations);
            }
        }
        return new Annotations(annotationsForField);
    }

    private static Annotations getAnnotationsForOutputField(FieldInfo fieldInfo, MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationsForField = getAnnotationsForField(fieldInfo, methodInfo);
        org.jboss.jandex.Type returnType = methodInfo.returnType();
        if (returnType != null) {
            Map<DotName, AnnotationInstance> returnTypeAnnotations = getAnnotationsWithFilter(returnType,
                    Annotations.DATE_FORMAT,
                    Annotations.NUMBER_FORMAT);
            if (!returnTypeAnnotations.isEmpty()) {
                annotationsForField.putAll(returnTypeAnnotations);
            }
        }
        return new Annotations(annotationsForField);
    }

    private static Map<DotName, AnnotationInstance> getAnnotations(org.jboss.jandex.Type type) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        if (type.kind().equals(org.jboss.jandex.Type.Kind.PARAMETERIZED_TYPE)) {
            org.jboss.jandex.Type typeInCollection = type.asParameterizedType().arguments().get(0);
            annotationMap.putAll(getAnnotations(typeInCollection));
        } else {
            List<AnnotationInstance> annotations = type.annotations();
            for (AnnotationInstance annotationInstance : annotations) {
                annotationMap.put(annotationInstance.name(), annotationInstance);
            }
        }

        return annotationMap;
    }

    private static Map<DotName, AnnotationInstance> getAnnotationsForType(org.jboss.jandex.Type type) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();
        for (AnnotationInstance annotationInstance : type.annotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }
        return annotationMap;
    }

    private static Map<DotName, AnnotationInstance> getAnnotationsForField(FieldInfo fieldInfo, MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();
        if (fieldInfo != null)
            annotationMap.putAll(listToMap(fieldInfo.annotations()));
        if (methodInfo != null)
            annotationMap.putAll(listToMap(methodInfo.annotations()));
        return annotationMap;
    }

    private static Map<DotName, AnnotationInstance> listToMap(List<AnnotationInstance> annotationInstances) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : annotationInstances) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }
        return annotationMap;
    }

    private static Map<DotName, AnnotationInstance> getAnnotationsWithFilter(org.jboss.jandex.Type type, DotName... filter) {
        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        if (type.kind().equals(org.jboss.jandex.Type.Kind.PARAMETERIZED_TYPE)) {
            org.jboss.jandex.Type typeInCollection = type.asParameterizedType().arguments().get(0);
            annotationMap.putAll(getAnnotationsWithFilter(typeInCollection, filter));
        } else {
            List<AnnotationInstance> annotations = type.annotations();
            for (AnnotationInstance annotationInstance : annotations) {
                if (Arrays.asList(filter).contains(annotationInstance.name())) {
                    annotationMap.put(annotationInstance.name(), annotationInstance);
                }
            }
        }

        return annotationMap;
    }

    private static final short ZERO = 0;

    // MicroProfile GraphQL Annotations
    public static final DotName GRAPHQL_API = DotName.createSimple("org.eclipse.microprofile.graphql.GraphQLApi");
    public static final DotName QUERY = DotName.createSimple("org.eclipse.microprofile.graphql.Query");
    public static final DotName MUTATION = DotName.createSimple("org.eclipse.microprofile.graphql.Mutation");
    public static final DotName INPUT = DotName.createSimple("org.eclipse.microprofile.graphql.Input");
    public static final DotName TYPE = DotName.createSimple("org.eclipse.microprofile.graphql.Type");
    public static final DotName INTERFACE = DotName.createSimple("org.eclipse.microprofile.graphql.Interface");
    public static final DotName ENUM = DotName.createSimple("org.eclipse.microprofile.graphql.Enum");
    public static final DotName ID = DotName.createSimple("org.eclipse.microprofile.graphql.Id");
    public static final DotName DESCRIPTION = DotName.createSimple("org.eclipse.microprofile.graphql.Description");
    public static final DotName DATE_FORMAT = DotName.createSimple("org.eclipse.microprofile.graphql.DateFormat");
    public static final DotName NUMBER_FORMAT = DotName.createSimple("org.eclipse.microprofile.graphql.NumberFormat");
    public static final DotName DEFAULT_VALUE = DotName.createSimple("org.eclipse.microprofile.graphql.DefaultValue");
    public static final DotName IGNORE = DotName.createSimple("org.eclipse.microprofile.graphql.Ignore");
    public static final DotName NON_NULL = DotName.createSimple("org.eclipse.microprofile.graphql.NonNull");
    public static final DotName NAME = DotName.createSimple("org.eclipse.microprofile.graphql.Name");
    public static final DotName SOURCE = DotName.createSimple("org.eclipse.microprofile.graphql.Source");

    // Json-B Annotations
    public static final DotName JSONB_DATE_FORMAT = DotName.createSimple("javax.json.bind.annotation.JsonbDateFormat");
    public static final DotName JSONB_NUMBER_FORMAT = DotName.createSimple("javax.json.bind.annotation.JsonbNumberFormat");
    public static final DotName JSONB_PROPERTY = DotName.createSimple("javax.json.bind.annotation.JsonbProperty");
    public static final DotName JSONB_TRANSIENT = DotName.createSimple("javax.json.bind.annotation.JsonbTransient");

    // Bean Validation Annotations (SmallRye extra, not part of the spec)
    public static final DotName BEAN_VALIDATION_NOT_NULL = DotName.createSimple("javax.validation.constraints.NotNull");
    public static final DotName BEAN_VALIDATION_NOT_EMPTY = DotName.createSimple("javax.validation.constraints.NotEmpty");
    public static final DotName BEAN_VALIDATION_NOT_BLANK = DotName.createSimple("javax.validation.constraints.NotBlank");

    // TODO: Also supprot OpenAPI Schema ??

}

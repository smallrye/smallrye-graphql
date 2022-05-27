package io.smallrye.graphql.schema;

import static java.util.Collections.emptyMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;

import io.smallrye.graphql.schema.helper.Direction;

/**
 * All the annotations we care about for a certain context
 * <p>
 * There are multiple static methods to create the annotations for the correct context
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Annotations {

    private final Map<DotName, AnnotationInstance> annotationsMap;

    public final Map<DotName, AnnotationInstance> parentAnnotations;

    /**
     * Get used when creating operations.
     * Operation only have methods (no properties)
     *
     * @param methodInfo the java method
     * @return Annotations for this method and its return-type
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

        final Type type = methodInfo.returnType();
        if (Classes.isParameterized(type)) {
            Type wrappedType = type.asParameterizedType().arguments().get(0);
            for (final AnnotationInstance annotationInstance : wrappedType.annotations()) {
                DotName name = annotationInstance.name();
                annotationMap.put(name, annotationInstance);
            }
        }

        Map<DotName, AnnotationInstance> parentAnnotations = getParentAnnotations(methodInfo.declaringClass());

        return new Annotations(annotationMap, parentAnnotations);
    }

    private static Map<DotName, AnnotationInstance> getParentAnnotations(FieldInfo fieldInfo, MethodInfo methodInfo) {
        ClassInfo declaringClass = fieldInfo != null ? fieldInfo.declaringClass() : methodInfo.declaringClass();
        return getParentAnnotations(declaringClass);
    }

    private static Map<DotName, AnnotationInstance> getParentAnnotations(ClassInfo classInfo) {
        Map<DotName, AnnotationInstance> parentAnnotations = new HashMap<>();

        for (AnnotationInstance classAnnotation : classInfo.classAnnotations()) {
            parentAnnotations.putIfAbsent(classAnnotation.name(), classAnnotation);
        }

        Map<DotName, AnnotationInstance> packageAnnotations = getPackageAnnotations(classInfo);
        for (DotName dotName : packageAnnotations.keySet()) {
            parentAnnotations.putIfAbsent(dotName, packageAnnotations.get(dotName));
        }

        return parentAnnotations;
    }

    private static Map<DotName, AnnotationInstance> getPackageAnnotations(ClassInfo classInfo) {
        Map<DotName, AnnotationInstance> packageAnnotations = new HashMap<>();

        DotName packageName = packageInfo(classInfo);
        if (packageName != null) {
            ClassInfo packageInfo = ScanningContext.getIndex().getClassByName(packageName);
            if (packageInfo != null) {
                for (AnnotationInstance packageAnnotation : packageInfo.classAnnotations()) {
                    packageAnnotations.putIfAbsent(packageAnnotation.name(), packageAnnotation);
                }
            }
        }

        return packageAnnotations;
    }

    private static DotName packageInfo(ClassInfo classInfo) {
        String className = classInfo.name().toString();
        int index = className.lastIndexOf('.');
        if (index == -1) {
            return null;
        }
        return DotName.createSimple(className.substring(0, index) + ".package-info");
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
     * This is used for public fields
     *
     * @param direction the direction
     * @param fieldInfo the java property
     * @return annotations for this field
     */
    public static Annotations getAnnotationsForPojo(Direction direction, FieldInfo fieldInfo) {
        return getAnnotationsForPojo(direction, fieldInfo, null);
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

    public static Annotations getAnnotationsForInputCreator(MethodInfo method, short position, FieldInfo fieldInfo) {
        Map<DotName, AnnotationInstance> annotationsForField = getAnnotationsForField(fieldInfo, null);

        if (fieldInfo != null) {
            annotationsForField.putAll(getTypeUseAnnotations(fieldInfo.type()));
        }
        annotationsForField.putAll(getAnnotationsForArgument(method, position).annotationsMap);

        Map<DotName, AnnotationInstance> parentAnnotations = getParentAnnotations(fieldInfo, method);

        return new Annotations(annotationsForField, parentAnnotations);
    }

    /**
     * Get used when we create types and references to them
     * <p>
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

        Map<DotName, AnnotationInstance> packageAnnotations = getPackageAnnotations(classInfo);
        for (DotName dotName : packageAnnotations.keySet()) {
            annotationMap.putIfAbsent(dotName, packageAnnotations.get(dotName));
        }

        return new Annotations(annotationMap, packageAnnotations);
    }

    /**
     * Get used when creating arrays.
     * <p>
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

        Map<DotName, AnnotationInstance> annotationMap = getAnnotations(parameterType);

        for (AnnotationInstance anno : methodInfo.annotations()) {
            if (anno.target().kind().equals(AnnotationTarget.Kind.METHOD_PARAMETER)) {
                MethodParameterInfo methodParameter = anno.target().asMethodParameter();
                short position = methodParameter.position();
                if (position == pos) {
                    annotationMap.put(anno.name(), anno);
                }
            }
        }

        final Map<DotName, AnnotationInstance> parentAnnotations = getParentAnnotations(methodInfo.declaringClass());

        return new Annotations(annotationMap, parentAnnotations);
    }

    public static boolean isJsonBAnnotation(AnnotationInstance instance) {
        return instance.name().toString().startsWith(JAKARTA_JSONB) || instance.name().toString().startsWith(JAVAX_JSONB);
    }

    // ------- All static creators done, now the actual class --------

    private Annotations(Map<DotName, AnnotationInstance> annotations) {
        this(annotations, new HashMap<>());
    }

    /**
     * Create the annotations, mapped by name
     *
     * @param annotations the annotation
     */
    private Annotations(Map<DotName, AnnotationInstance> annotations, Map<DotName, AnnotationInstance> parentAnnotations) {
        this.annotationsMap = annotations;
        this.parentAnnotations = parentAnnotations;
    }

    public Set<DotName> getAnnotationNames() {
        return annotationsMap.keySet();
    }

    public Annotations removeAnnotations(DotName... annotations) {
        Map<DotName, AnnotationInstance> newAnnotationsMap = new HashMap<>(annotationsMap);
        for (DotName annotation : annotations) {
            newAnnotationsMap.remove(annotation);
        }
        return new Annotations(newAnnotationsMap, this.parentAnnotations);
    }

    /**
     * Get a specific annotation
     *
     * @param annotation the annotation you want
     * @return the annotation value or null
     */
    public AnnotationValue getAnnotationValue(DotName annotation) {
        return this.annotationsMap.get(annotation).value();
    }

    /**
     * Get a specific annotation
     *
     * @param annotation the annotation you want
     * @return the annotation value or null
     */
    public AnnotationValue getAnnotationValue(DotName annotation, String name) {
        return this.annotationsMap.get(annotation).value(name);
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

    public boolean containsOneOfTheseInheritableAnnotations(DotName... annotations) {
        for (DotName name : annotations) {
            if (this.parentAnnotations.containsKey(name)) {
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

    @Override
    public String toString() {
        return annotationsMap.toString();
    }

    private boolean hasValidMethodAnnotation(DotName annotation) {
        return containsKeyAndValidValue(annotation) && isMethodAnnotation(getAnnotation(annotation));
    }

    private boolean hasValidMethodParameterAnnotation(DotName annotation) {
        return containsKeyAndValidValue(annotation) && isMethodParameterAnnotation(getAnnotation(annotation));
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
        String value;
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

        if (fieldInfo != null) {
            annotationsForField.putAll(getTypeUseAnnotations(fieldInfo.type()));
        }
        if (methodInfo != null) {
            List<org.jboss.jandex.Type> parameters = methodInfo.parameters();
            if (!parameters.isEmpty()) {
                org.jboss.jandex.Type param = parameters.get(ZERO);
                annotationsForField.putAll(getTypeUseAnnotations(param));
            }
        }

        final Map<DotName, AnnotationInstance> parentAnnotations = getParentAnnotations(fieldInfo, methodInfo);

        return new Annotations(annotationsForField, parentAnnotations);
    }

    private static Annotations getAnnotationsForOutputField(FieldInfo fieldInfo, MethodInfo methodInfo) {
        Map<DotName, AnnotationInstance> annotationsForField = getAnnotationsForField(fieldInfo, methodInfo);

        if (fieldInfo != null) {
            annotationsForField.putAll(getTypeUseAnnotations(fieldInfo.type()));
        }
        if (methodInfo != null) {
            org.jboss.jandex.Type returnType = methodInfo.returnType();
            if (returnType != null) {
                annotationsForField.putAll(getTypeUseAnnotations(methodInfo.returnType()));
            }
        }

        Map<DotName, AnnotationInstance> parentAnnotations = getParentAnnotations(fieldInfo, methodInfo);

        return new Annotations(annotationsForField, parentAnnotations);
    }

    private static Map<DotName, AnnotationInstance> getTypeUseAnnotations(org.jboss.jandex.Type type) {
        if (type != null) {
            return getAnnotationsWithFilter(type,
                    Annotations.DATE_FORMAT,
                    Annotations.NUMBER_FORMAT);
        }
        return emptyMap();
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
            annotationMap.putAll(listToMap(fieldInfo.annotations().stream()
                    .filter(ai -> ai.target().kind() == Kind.FIELD)
                    .collect(Collectors.toList())));
        if (methodInfo != null)
            annotationMap.putAll(listToMap(methodInfo.annotations().stream()
                    .filter(ai -> ai.target().kind() == Kind.METHOD || ai.target().kind() == Kind.METHOD_PARAMETER)
                    .collect(Collectors.toList())));
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

    // SmallRye Common Annotations
    public static final DotName BLOCKING = DotName.createSimple("io.smallrye.common.annotation.Blocking");
    public static final DotName NON_BLOCKING = DotName.createSimple("io.smallrye.common.annotation.NonBlocking");

    // SmallRye GraphQL Annotations (Experimental)
    public static final DotName TO_SCALAR = DotName.createSimple("io.smallrye.graphql.api.ToScalar"); // TODO: Remove
    public static final DotName ADAPT_TO_SCALAR = DotName.createSimple("io.smallrye.graphql.api.AdaptToScalar");
    public static final DotName ADAPT_WITH = DotName.createSimple("io.smallrye.graphql.api.AdaptWith");
    public static final DotName ERROR_CODE = DotName.createSimple("io.smallrye.graphql.api.ErrorCode");
    public static final DotName DATAFETCHER = DotName.createSimple("io.smallrye.graphql.api.DataFetcher");
    public static final DotName SUBCRIPTION = DotName.createSimple("io.smallrye.graphql.api.Subscription");
    public static final DotName DIRECTIVE = DotName.createSimple("io.smallrye.graphql.api.Directive");
    public static final DotName DEFAULT_NON_NULL = DotName.createSimple("io.smallrye.graphql.api.DefaultNonNull");
    public static final DotName NULLABLE = DotName.createSimple("io.smallrye.graphql.api.Nullable");
    public static final DotName RESULT = DotName.createSimple("io.smallrye.graphql.api.GraphQLResult");

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
    public static final String JAVAX_JSONB = "javax.json.bind.annotation.";
    public static final DotName JAVAX_JSONB_DATE_FORMAT = DotName.createSimple(JAVAX_JSONB + "JsonbDateFormat");
    public static final DotName JAVAX_JSONB_NUMBER_FORMAT = DotName.createSimple(JAVAX_JSONB + "JsonbNumberFormat");
    public static final DotName JAVAX_JSONB_PROPERTY = DotName.createSimple(JAVAX_JSONB + "JsonbProperty");
    public static final DotName JAVAX_JSONB_TRANSIENT = DotName.createSimple(JAVAX_JSONB + "JsonbTransient");
    public static final DotName JAVAX_JSONB_CREATOR = DotName.createSimple(JAVAX_JSONB + "JsonbCreator");
    public static final DotName JAVAX_JSONB_TYPE_ADAPTER = DotName.createSimple(JAVAX_JSONB + "JsonbTypeAdapter");

    public static final String JAKARTA_JSONB = "jakarta.json.bind.annotation.";
    public static final DotName JAKARTA_JSONB_DATE_FORMAT = DotName.createSimple(JAKARTA_JSONB + "JsonbDateFormat");
    public static final DotName JAKARTA_JSONB_NUMBER_FORMAT = DotName.createSimple(JAKARTA_JSONB + "JsonbNumberFormat");
    public static final DotName JAKARTA_JSONB_PROPERTY = DotName.createSimple(JAKARTA_JSONB + "JsonbProperty");
    public static final DotName JAKARTA_JSONB_TRANSIENT = DotName.createSimple(JAKARTA_JSONB + "JsonbTransient");
    public static final DotName JAKARTA_JSONB_CREATOR = DotName.createSimple(JAKARTA_JSONB + "JsonbCreator");
    public static final DotName JAKARTA_JSONB_TYPE_ADAPTER = DotName.createSimple(JAKARTA_JSONB + "JsonbTypeAdapter");

    // Jackson Annotations
    public static final DotName JACKSON_IGNORE = DotName.createSimple("com.fasterxml.jackson.annotation.JsonIgnore");
    public static final DotName JACKSON_PROPERTY = DotName.createSimple("com.fasterxml.jackson.annotation.JsonProperty");
    public static final DotName JACKSON_CREATOR = DotName.createSimple("com.fasterxml.jackson.annotation.JsonCreator");
    public static final DotName JACKSON_FORMAT = DotName.createSimple("com.fasterxml.jackson.annotation.JsonFormat");

    // Bean Validation Annotations (SmallRye extra, not part of the spec)
    public static final DotName JAVAX_BEAN_VALIDATION_NOT_NULL = DotName.createSimple("javax.validation.constraints.NotNull");
    public static final DotName JAVAX_BEAN_VALIDATION_NOT_EMPTY = DotName.createSimple("javax.validation.constraints.NotEmpty");
    public static final DotName JAVAX_BEAN_VALIDATION_NOT_BLANK = DotName.createSimple("javax.validation.constraints.NotBlank");

    public static final DotName JAKARTA_BEAN_VALIDATION_NOT_NULL = DotName
            .createSimple("jakarta.validation.constraints.NotNull");
    public static final DotName JAKARTA_BEAN_VALIDATION_NOT_EMPTY = DotName
            .createSimple("jakarta.validation.constraints.NotEmpty");
    public static final DotName JAKARTA_BEAN_VALIDATION_NOT_BLANK = DotName
            .createSimple("jakarta.validation.constraints.NotBlank");

    //Kotlin NotNull
    public static final DotName KOTLIN_NOT_NULL = DotName.createSimple("org.jetbrains.annotations.NotNull");

}

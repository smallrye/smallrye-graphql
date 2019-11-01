package io.smallrye.graphql.type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.index.Classes;
import io.smallrye.graphql.inspector.MethodArgumentInspector;
import io.smallrye.graphql.scalar.GraphQLScalarTypeCreator;

/**
 * Create some Maps of all the types.
 * This looks at all POJOs annotated with @Type or @InputType
 * and then also all return type and arguments on @Queries and @Mutations.
 * 
 * It produces a few maps, that can be injected anywhere in the code:
 * - outputObjectMap - contains all object type used for responses.
 * - inputObjectMap - contains all object type used for requests.
 * - enumMap - contains all enum types.
 * TODO:
 * - interfaceMap - contains all interface types
 * - unionMap - contains all union types
 * - Generics ?
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class TypeMappingInitializer {
    private static final Logger LOG = Logger.getLogger(TypeMappingInitializer.class.getName());

    @Inject
    private Index index;

    @Inject
    private GraphQLScalarTypeCreator graphQLScalarTypeCreator;

    @Inject
    private MethodArgumentInspector methodArgumentInspector;

    @Produces
    private final Map<DotName, GraphQLInputObjectType> inputObjectMap = new HashMap<>();

    @Produces
    private final Map<DotName, GraphQLObjectType> outputObjectMap = new HashMap<>();

    @Produces
    private final Map<DotName, GraphQLEnumType> enumMap = new HashMap<>();

    private final Map<DotName, TypeHolder> inputClassesWeCareAbout = new HashMap<>();
    private final Map<DotName, TypeHolder> outputClassesWeCareAbout = new HashMap<>();
    private final Map<DotName, TypeHolder> enumsWeCareAbout = new HashMap<>();

    private enum Direction {
        IN,
        OUT
    }

    @PostConstruct
    void init() {

        // First traverse all the types and create referenced types
        scanClassLevelAnnotations(Direction.OUT, Annotations.TYPE);
        scanClassLevelAnnotations(Direction.IN, Annotations.INPUTTYPE);
        scanMethodLevelAnnotations(Annotations.QUERY);
        scanMethodLevelAnnotations(Annotations.MUTATION);

        // TODO: Can a enum be named different between in- and output ?
        for (Map.Entry<DotName, TypeHolder> e : enumsWeCareAbout.entrySet()) {
            this.enumMap.put(e.getKey(), createEnumType(e.getValue()));
            LOG.debug("adding [" + e.getKey() + "] to the enums list");
        }

        for (Map.Entry<DotName, TypeHolder> e : outputClassesWeCareAbout.entrySet()) {
            this.outputObjectMap.put(e.getKey(), createOutputObjectType(e.getValue()));
            LOG.debug("adding [" + e.getKey() + "] to the output object list");
        }

        for (Map.Entry<DotName, TypeHolder> e : inputClassesWeCareAbout.entrySet()) {
            this.inputObjectMap.put(e.getKey(), createInputObjectType(e.getValue()));
            LOG.debug("adding [" + e.getKey() + "] to the input object list");
        }

        inputClassesWeCareAbout.clear();
        outputClassesWeCareAbout.clear();
        enumsWeCareAbout.clear();
    }

    private void scanClassLevelAnnotations(Direction direction, DotName annotationName) {
        List<AnnotationInstance> annotations = this.index.getAnnotations(annotationName);
        for (AnnotationInstance annotation : annotations) {

            if (annotation.target().kind().equals(AnnotationTarget.Kind.CLASS)) {
                ClassInfo classInfo = annotation.target().asClass();
                scanClass(direction, classInfo);
            }
        }
    }

    private void scanMethodLevelAnnotations(DotName annotationName) {
        List<AnnotationInstance> annotations = this.index.getAnnotations(annotationName);

        for (AnnotationInstance annotation : annotations) {
            switch (annotation.target().kind()) {
                case METHOD:
                    MethodInfo methodInfo = annotation.target().asMethod();
                    // Return types on Queries and Mutations
                    // TODO: What if getting has void ?
                    if (!methodInfo.returnType().kind().equals(Type.Kind.VOID)) {
                        scanType(Direction.OUT, methodInfo.returnType());
                    }
                    // arguments on getters and setter
                    List<Type> parameters = methodInfo.parameters();
                    for (Type parameter : parameters) {
                        scanType(Direction.IN, parameter);
                    }
                    break;
            }
        }
    }

    private void scanClass(Direction direction, ClassInfo classInfo) {
        String name = getName(direction, classInfo);
        if (Classes.isEnum(classInfo)) {
            if (!enumsWeCareAbout.containsKey(classInfo.name())) {
                enumsWeCareAbout.put(classInfo.name(), new TypeHolder(name, classInfo));
            }
        } else {
            if (Direction.OUT.equals(direction) && !outputClassesWeCareAbout.containsKey(classInfo.name())) {
                outputClassesWeCareAbout.put(classInfo.name(), new TypeHolder(name, classInfo));
                scanFieldsAndMethods(direction, classInfo);
            } else if (Direction.IN.equals(direction) && !inputClassesWeCareAbout.containsKey(classInfo.name())) {
                inputClassesWeCareAbout.put(classInfo.name(), new TypeHolder(name, classInfo));
                scanFieldsAndMethods(direction, classInfo);
            }
        }
    }

    private String getName(Direction direction, ClassInfo classInfo) {
        if (Direction.OUT.equals(direction)) {
            return getOutputNameFromClass(classInfo);
        } else {
            return getInputNameFromClass(classInfo);
        }
    }

    private void scanFieldsAndMethods(Direction direction, ClassInfo classInfo) {
        // fields
        List<FieldInfo> fieldInfos = classInfo.fields();
        for (FieldInfo fieldInfo : fieldInfos) {
            Type type = fieldInfo.type();
            scanType(direction, type);
        }

        // methods
        List<MethodInfo> methodInfos = classInfo.methods();
        for (MethodInfo methodInfo : methodInfos) {
            String methodName = methodInfo.name();

            // return types on getters and setters
            if (isSetter(methodName) || isGetter(methodName)) {
                // TODO: What if getting has void ?
                if (!methodInfo.returnType().kind().equals(Type.Kind.VOID)) {
                    scanType(direction, methodInfo.returnType());
                }

                // arguments on getters and setter
                List<Type> parameters = methodInfo.parameters();
                for (Type parameter : parameters) {
                    scanType(direction, parameter);
                }
            }
        }
    }

    private void scanType(Direction direction, Type type) {
        switch (type.kind()) {
            case ARRAY:
                Type typeInArray = type.asArrayType().component();
                scanType(direction, typeInArray);
                break;
            case PARAMETERIZED_TYPE:
                // TODO: Check if there is more than one type in the Collection, throw an exception ?
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                scanType(direction, typeInCollection);
                break;
            case PRIMITIVE:
                if (!graphQLScalarTypeCreator.isScalarType(type.name())) {
                    LOG.warn("No scalar mapping for " + type.name() + " with kind " + type.kind());
                }
                break;
            case CLASS:
                if (!graphQLScalarTypeCreator.isScalarType(type.name())) {
                    ClassInfo classInfo = index.getClassByName(type.name());
                    if (classInfo != null) {
                        scanClass(direction, classInfo);
                    } else {
                        LOG.warn("Not indexed class " + type.name() + " with kind " + type.kind());
                    }
                }
                break;
            default:
                LOG.error("What should we do with field type of " + type.name() + " with kind " + type.kind());
                break;
        }
    }

    private boolean isSetter(String methodName) {
        return methodName.length() > 3 && methodName.startsWith(SET);
    }

    private boolean isGetter(String methodName) {
        return (methodName.length() > 3 && methodName.startsWith(GET))
                || (methodName.length() > 2 && methodName.startsWith(IS));
    }

    // TODO: Test a more complex enum
    private GraphQLEnumType createEnumType(TypeHolder typeHolder) {
        String name = typeHolder.getNameInSchema();
        ClassInfo classInfo = typeHolder.getClassInfo();

        GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum()
                .name(name);

        // Description
        Optional<String> maybeDescription = getDescription(classInfo);
        if (maybeDescription.isPresent()) {
            builder = builder.description(maybeDescription.get());
        }

        // Values
        List<FieldInfo> fields = classInfo.fields();
        for (FieldInfo field : fields) {
            if (!field.type().kind().equals(Type.Kind.ARRAY)) {
                builder = builder.value(field.name());
            }
        }
        return builder.build();
    }

    private GraphQLObjectType createOutputObjectType(TypeHolder typeHolder) {
        String name = typeHolder.getNameInSchema();
        ClassInfo classInfo = typeHolder.getClassInfo();

        GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject();
        objectTypeBuilder = objectTypeBuilder.name(name);

        // Description
        Optional<String> maybeDescription = getDescription(classInfo);
        if (maybeDescription.isPresent()) {
            objectTypeBuilder = objectTypeBuilder.description(maybeDescription.get());
        }

        // Fields
        objectTypeBuilder = objectTypeBuilder.fields(getGraphQLFieldDefinitions(classInfo, name));

        return objectTypeBuilder.build();
    }

    private GraphQLInputObjectType createInputObjectType(TypeHolder typeHolder) {
        String name = typeHolder.getNameInSchema();
        ClassInfo classInfo = typeHolder.getClassInfo();

        GraphQLInputObjectType.Builder inputObjectTypeBuilder = GraphQLInputObjectType.newInputObject().name(name);

        // Description
        Optional<String> maybeDescription = getDescription(classInfo);
        if (maybeDescription.isPresent()) {
            inputObjectTypeBuilder = inputObjectTypeBuilder.description(maybeDescription.get());
        }

        // Fields
        inputObjectTypeBuilder = inputObjectTypeBuilder.fields(getGraphQLInputObjectField(classInfo, name));

        return inputObjectTypeBuilder.build();
    }

    private List<GraphQLFieldDefinition> getGraphQLFieldDefinitions(ClassInfo classInfo, String name) {
        List<GraphQLFieldDefinition> fieldDefinitions = new ArrayList<>();
        // Fields (TODO: Look at methods rather ? Or both ?)
        List<FieldInfo> fields = classInfo.fields();

        for (FieldInfo field : fields) {
            // Check if we should we ignore this ?
            if (!shouldIgnore(field)) {
                // Check if there is a getter (for output) 
                Optional<MethodInfo> maybeGetter = getGetMethod(field.name(), classInfo);
                if (maybeGetter.isPresent()) {
                    MethodInfo getter = maybeGetter.get();
                    if (!shouldIgnore(getter)) {
                        GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
                        // Annotations on the field and setter
                        Map<DotName, AnnotationInstance> annotationsForThisField = getAnnotationsForThisField(field, getter);

                        // Name
                        builder = builder.name(getOutputNameForField(annotationsForThisField, field.name()));
                        // Description
                        Optional<String> maybeFieldDescription = getDescription(annotationsForThisField, field.type());
                        if (maybeFieldDescription.isPresent()) {
                            builder = builder.description(maybeFieldDescription.get());
                        }

                        // Type
                        Type type = field.type();
                        DotName fieldTypeName = type.name();
                        GraphQLOutputType graphQLOutputType;
                        if (fieldTypeName.equals(classInfo.name())) {
                            // Myself
                            if (markAsNonNull(field, getter)) {
                                graphQLOutputType = GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef(name));
                            } else {
                                graphQLOutputType = GraphQLTypeReference.typeRef(name);
                            }
                        } else {
                            // Another type    
                            if (markAsNonNull(field, getter)) {
                                graphQLOutputType = GraphQLNonNull.nonNull(toGraphQLOutputType(type, annotationsForThisField));
                            } else {
                                graphQLOutputType = toGraphQLOutputType(type, annotationsForThisField);
                            }
                        }
                        builder = builder.type(graphQLOutputType);

                        fieldDefinitions.add(builder.build());
                    }
                }
            }
        }
        return fieldDefinitions;
    }

    private List<GraphQLInputObjectField> getGraphQLInputObjectField(ClassInfo classInfo, String name) {
        List<GraphQLInputObjectField> inputObjectFields = new ArrayList<>();
        // Fields (TODO: Look at methods rather ? Or both ?)
        List<FieldInfo> fields = classInfo.fields();
        short count = 0;
        for (FieldInfo field : fields) {
            // Check if we should we ignore this ?
            if (!shouldIgnore(field)) {

                // Check if there is a setter (for input) 
                Optional<MethodInfo> maybeSetter = getSetMethod(field.name(), classInfo);
                if (maybeSetter.isPresent()) {
                    MethodInfo setter = maybeSetter.get();
                    if (!shouldIgnore(setter)) {
                        GraphQLInputObjectField.Builder builder = GraphQLInputObjectField.newInputObjectField();
                        // Annotations on the field and setter
                        Map<DotName, AnnotationInstance> annotationsForThisField = getAnnotationsForThisField(field, setter);

                        // Name
                        builder = builder.name(getInputNameForField(annotationsForThisField, field.name()));

                        // Description
                        Optional<String> maybeFieldDescription = getDescription(annotationsForThisField, field.type());
                        if (maybeFieldDescription.isPresent()) {
                            builder = builder.description(maybeFieldDescription.get());
                        }
                        // Type
                        Type type = field.type();
                        DotName fieldTypeName = type.name();
                        GraphQLInputType graphQLInputType;
                        if (fieldTypeName.equals(classInfo.name())) {
                            // Myself
                            if (markAsNonNull(field, setter)) {
                                graphQLInputType = GraphQLNonNull.nonNull(GraphQLTypeReference.typeRef(name));
                            } else {
                                graphQLInputType = GraphQLTypeReference.typeRef(name);
                            }
                        } else {
                            // Another type
                            if (markAsNonNull(field, setter)) {
                                graphQLInputType = GraphQLNonNull.nonNull(toGraphQLInputType(type, annotationsForThisField));
                            } else {
                                graphQLInputType = toGraphQLInputType(type, annotationsForThisField);
                            }
                        }

                        builder = builder.type(graphQLInputType);

                        // Default value (on method)
                        Optional<AnnotationValue> maybeDefaultValue = methodArgumentInspector.getArgumentAnnotationValue(setter,
                                count, Annotations.DEFAULT_VALUE);
                        if (maybeDefaultValue.isPresent()) {
                            builder = builder.defaultValue(maybeDefaultValue.get().value());
                        } else {
                            // Default value (on field)
                            Optional<AnnotationValue> maybeFieldAnnotation = getAnnotationValue(field.annotations(),
                                    Annotations.DEFAULT_VALUE);
                            if (maybeFieldAnnotation.isPresent()) {
                                builder = builder.defaultValue(maybeFieldAnnotation.get().value());
                            }
                        }

                        inputObjectFields.add(builder.build());
                    }
                }
            }
            count++;
        }
        return inputObjectFields;
    }

    private GraphQLOutputType toGraphQLOutputType(Type type, Map<DotName, AnnotationInstance> annotations) {
        DotName fieldTypeName = type.name();

        if (graphQLScalarTypeCreator.isScalarType(fieldTypeName)) {
            // Scalar
            return graphQLScalarTypeCreator.getGraphQLScalarType(fieldTypeName, annotations);
        } else if (enumMap.containsKey(fieldTypeName)) {
            // Enum  
            return enumMap.get(fieldTypeName);
        } else if (type.kind().equals(Type.Kind.ARRAY)) {
            // Array 
            Type typeInArray = type.asArrayType().component();
            return GraphQLList.list(toGraphQLOutputType(typeInArray, annotations));
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            // TODO: Check if there is more than one type in the Collection, throw an exception ?
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return GraphQLList.list(toGraphQLOutputType(typeInCollection, annotations));
        } else if (outputClassesWeCareAbout.containsKey(type.name())) {
            // Reference to some type
            GraphQLTypeReference graphQLTypeReference = outputClassesWeCareAbout.get(type.name()).getGraphQLTypeReference();
            return graphQLTypeReference;
        } else {
            // Maps ? Intefaces ? Generics ?
            throw new RuntimeException("Don't know what to do with " + type);
        }
    }

    private GraphQLInputType toGraphQLInputType(Type type, Map<DotName, AnnotationInstance> annotations) {
        DotName fieldTypeName = type.name();

        if (graphQLScalarTypeCreator.isScalarType(fieldTypeName)) {
            // Scalar
            return graphQLScalarTypeCreator.getGraphQLScalarType(fieldTypeName, annotations);
        } else if (enumMap.containsKey(fieldTypeName)) {
            // Enum  
            return enumMap.get(fieldTypeName);
        } else if (type.kind().equals(Type.Kind.ARRAY)) {
            // Array 
            Type typeInArray = type.asArrayType().component();
            return GraphQLList.list(toGraphQLInputType(typeInArray, annotations));
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            // TODO: Check if there is more than one type in the Collection, throw an exception ?
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return GraphQLList.list(toGraphQLInputType(typeInCollection, annotations));
        } else if (inputClassesWeCareAbout.containsKey(type.name())) {
            // Reference to some type
            GraphQLTypeReference graphQLTypeReference = inputClassesWeCareAbout.get(type.name()).getGraphQLTypeReference();
            return graphQLTypeReference;
        } else {
            // Maps ? Intefaces ? Generics ?
            throw new RuntimeException("Don't know what to do with " + type);
        }
    }

    private Optional<String> getDescription(ClassInfo classInfo) {
        Optional<AnnotationInstance> maybeDescription = findAnnotationInstance(classInfo.classAnnotations(),
                Annotations.DESCRIPTION);
        if (maybeDescription.isPresent()) {
            return getDescription(maybeDescription.get());
        }
        return Optional.empty();
    }

    private Optional<String> getDescription(Map<DotName, AnnotationInstance> annotationsForThisField, Type type) {
        if (containsKeyAndValidValue(annotationsForThisField, Annotations.DESCRIPTION)) {
            return Optional.of(annotationsForThisField.get(Annotations.DESCRIPTION).value().asString());
        } else if (isDateLikeTypeOrCollectionThereOf(type)) {
            if (containsKeyAndValidValue(annotationsForThisField, Annotations.JSONB_DATE_FORMAT)) {
                return Optional.of(annotationsForThisField.get(Annotations.JSONB_DATE_FORMAT).value().asString());
            } else {
                // return the default dates format
                if (type.name().equals(Classes.LOCALDATE)) {
                    return Optional.of(ISO_DATE);
                } else if (type.name().equals(Classes.LOCALTIME)) {
                    return Optional.of(ISO_TIME);
                } else if (type.name().equals(Classes.LOCALDATETIME)) {
                    return Optional.of(ISO_DATE_TIME);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<String> getDescription(AnnotationInstance descriptionAnnotation) {
        if (descriptionAnnotation != null) {
            AnnotationValue value = descriptionAnnotation.value();
            if (value != null) {
                return Optional.of(value.asString());
            }
        }
        return Optional.empty();
    }

    private boolean isDateLikeTypeOrCollectionThereOf(Type type) {
        switch (type.kind()) {
            case PARAMETERIZED_TYPE:
                // Collections
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return isDateLikeTypeOrCollectionThereOf(typeInCollection);
            case ARRAY:
                // Array
                Type typeInArray = type.asArrayType().component();
                return isDateLikeTypeOrCollectionThereOf(typeInArray);
            default:
                return type.name().equals(Classes.LOCALDATE)
                        || type.name().equals(Classes.LOCALTIME)
                        || type.name().equals(Classes.LOCALDATETIME);
        }
    }

    private Optional<AnnotationInstance> findAnnotationInstance(Collection<AnnotationInstance> annotations,
            DotName lookingFor) {
        if (annotations != null) {
            for (AnnotationInstance annotationInstance : annotations) {
                if (annotationInstance.name().equals(lookingFor))
                    return Optional.of(annotationInstance);
            }
        }
        return Optional.empty();
    }

    private Map<DotName, AnnotationInstance> getAnnotationsForThisField(FieldInfo fieldInfo, MethodInfo methodInfo) {

        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : fieldInfo.annotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }

        for (AnnotationInstance annotationInstance : methodInfo.annotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }

        return annotationMap;
    }

    // TODO: Where does @Argument fits in ?
    private String getInputNameForField(Map<DotName, AnnotationInstance> annotationsForThisField, String defaultValue) {
        if (containsKeyAndValidValue(annotationsForThisField, Annotations.INPUTFIELD)) {
            return annotationsForThisField.get(Annotations.INPUTFIELD).value().asString();
        } else if (containsKeyAndValidValue(annotationsForThisField, Annotations.JSONB_PROPERTY)) {
            return annotationsForThisField.get(Annotations.JSONB_PROPERTY).value().asString();
        }
        return defaultValue;
    }

    private String getOutputNameForField(Map<DotName, AnnotationInstance> annotationsForThisField, String defaultValue) {
        if (containsKeyAndValidValue(annotationsForThisField, Annotations.JSONB_PROPERTY)) {
            return annotationsForThisField.get(Annotations.JSONB_PROPERTY).value().asString();
        } else if (containsKeyAndValidValue(annotationsForThisField, Annotations.QUERY)) {
            return annotationsForThisField.get(Annotations.QUERY).value().asString();
        }

        return defaultValue;
    }

    private boolean containsKeyAndValidValue(Map<DotName, AnnotationInstance> annotations, DotName key) {
        return annotations.containsKey(key) && annotations.get(key).value() != null;
    }

    private Optional<AnnotationValue> getAnnotationValue(Collection<AnnotationInstance> annotations, DotName namedAnnotation) {
        Optional<AnnotationInstance> maybeAnnotation = findAnnotationInstance(annotations,
                namedAnnotation);
        if (maybeAnnotation.isPresent()) {
            AnnotationInstance annotation = maybeAnnotation.get();
            if (annotation != null && annotation.value() != null) {
                return Optional.of(annotation.value());
            }
        }
        return Optional.empty();
    }

    private String getOutputNameFromClass(ClassInfo classInfo) {
        // check if there is a @Type annotation that contains a value
        Optional<String> maybeTypeValue = getClassAnnotationStringValue(classInfo, Annotations.TYPE);
        if (maybeTypeValue.isPresent()) {
            return maybeTypeValue.get();
        }
        // TODO: Do we support any other annotations ?
        return classInfo.name().local();
    }

    private String getInputNameFromClass(ClassInfo classInfo) {
        // check if there is an annotation that contains a name
        Optional<String> maybeTypeValue = getClassAnnotationStringValue(classInfo, Annotations.INPUTTYPE);
        if (maybeTypeValue.isPresent()) {
            return maybeTypeValue.get();
        }
        // TODO: Do we support any other annotations ?
        return classInfo.name().local() + "Input";
    }

    private Optional<String> getClassAnnotationStringValue(ClassInfo classInfo, DotName annotationName) {
        if (classInfo.annotations().containsKey(annotationName)) {
            List<AnnotationInstance> annotations = classInfo.annotations().get(annotationName);
            // TODO: What do we do if there is more than one
            AnnotationInstance typeAnnotation = annotations.get(0);
            if (typeAnnotation.value() != null) {
                return Optional.of(typeAnnotation.value().asString());
            }
        }
        return Optional.empty();
    }

    private boolean shouldIgnore(FieldInfo fieldInfo) {
        List<AnnotationInstance> annotations = fieldInfo.annotations();
        return shouldIgnore(annotations);
    }

    private boolean shouldIgnore(MethodInfo methodInfo) {
        List<AnnotationInstance> annotations = methodInfo.annotations();
        return shouldIgnore(annotations);
    }

    private boolean shouldIgnore(List<AnnotationInstance> annotations) {
        for (AnnotationInstance a : annotations) {
            if (a.name().equals(Annotations.IGNORE) || a.name().equals(Annotations.JSONB_TRANSIENT)) {
                return true;
            }
        }
        return false;
    }

    private boolean markAsNonNull(FieldInfo fieldInfo, MethodInfo methodInfo) {
        // check if the @NonNull annotation is present
        boolean hasNonNull = hasNonNull(fieldInfo, methodInfo);
        // check if the @DefaultValue annotation is present
        boolean hasDefaultValue = hasDefaultValue(fieldInfo, methodInfo);
        if (hasDefaultValue) {
            if (hasNonNull) {
                LOG.warn("Ignoring @NonNull on [" + methodInfo.name() + "] as there is a @DefaultValue");
            }
            return false;
        }

        return hasNonNull;
    }

    private boolean hasNonNull(FieldInfo fieldInfo, MethodInfo methodInfo) {
        return hasNonNull(methodInfo) || hasNonNull(fieldInfo);
    }

    private boolean hasNonNull(FieldInfo fieldInfo) {
        List<AnnotationInstance> annotations = fieldInfo.annotations();
        return hasNonNull(annotations);
    }

    private boolean hasNonNull(MethodInfo methodInfo) {
        List<AnnotationInstance> annotations = methodInfo.annotations();
        return hasNonNull(annotations);
    }

    private boolean hasNonNull(List<AnnotationInstance> annotations) {
        for (AnnotationInstance a : annotations) {
            // GraphQL Annotation, and optional Bean validation
            if (a.name().equals(Annotations.NON_NULL)
                    || a.name().equals(Annotations.BEAN_VALIDATION_NOT_NULL)
                    || a.name().equals(Annotations.BEAN_VALIDATION_NOT_EMPTY)
                    || a.name().equals(Annotations.BEAN_VALIDATION_NOT_BLANK)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasDefaultValue(FieldInfo fieldInfo, MethodInfo methodInfo) {
        return hasDefaultValue(methodInfo) || hasDefaultValue(fieldInfo);
    }

    private boolean hasDefaultValue(FieldInfo fieldInfo) {
        List<AnnotationInstance> annotations = fieldInfo.annotations();
        return hasDefaultValue(annotations);
    }

    private boolean hasDefaultValue(MethodInfo methodInfo) {
        List<AnnotationInstance> annotations = methodInfo.annotations();
        return hasDefaultValue(annotations);
    }

    private boolean hasDefaultValue(List<AnnotationInstance> annotations) {
        for (AnnotationInstance a : annotations) {
            if (a.name().equals(Annotations.DEFAULT_VALUE)) {
                return true;
            }
        }
        return false;
    }

    private Optional<MethodInfo> getSetMethod(String forField, ClassInfo classInfo) {
        String name = SET + forField;
        List<MethodInfo> methods = classInfo.methods();
        for (MethodInfo methodInfo : methods) {
            if (methodInfo.name().equalsIgnoreCase(name)) {
                return Optional.of(methodInfo);
            }
        }
        return Optional.empty();
    }

    private Optional<MethodInfo> getGetMethod(String forField, ClassInfo classInfo) {
        String get = GET + forField;
        String is = IS + forField;
        List<MethodInfo> methods = classInfo.methods();
        for (MethodInfo methodInfo : methods) {
            if (methodInfo.name().equalsIgnoreCase(get) || methodInfo.name().equalsIgnoreCase(is)) {
                return Optional.of(methodInfo);
            }
        }
        return Optional.empty();
    }

    private static final String SET = "set";
    private static final String GET = "get";
    private static final String IS = "is";

    private static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String ISO_DATE = "yyyy-MM-dd";
    private static final String ISO_TIME = "HH:mm:ss";
}

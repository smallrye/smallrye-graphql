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
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.index.Classes;

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
 * - inputObjectMap - contains all inputObject types
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
    private Map<DotName, GraphQLScalarType> scalarMap;

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

        for (Map.Entry<DotName, TypeHolder> e : enumsWeCareAbout.entrySet()) {
            this.enumMap.put(e.getKey(), createEnumType(e.getValue()));
            LOG.info("adding [" + e.getKey() + "] to the enums list");
        }

        for (Map.Entry<DotName, TypeHolder> e : outputClassesWeCareAbout.entrySet()) {
            this.outputObjectMap.put(e.getKey(), createOutputObjectType(e.getValue()));
            LOG.info("adding [" + e.getKey() + "] to the output object list");
        }

        for (Map.Entry<DotName, TypeHolder> e : inputClassesWeCareAbout.entrySet()) {
            this.inputObjectMap.put(e.getKey(), createInputObjectType(e.getValue()));
            LOG.info("adding [" + e.getKey() + "] to the input object list");
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
                case CLASS:
                    // TODO: Do we allow Query and Mutation on class level ?
                    break;
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
        // TODO: Get name from Annotation (Can name be from InputType, Type or JSonbProperty ? Or somewhere else ?)
        if (Direction.OUT.equals(direction)) {
            return classInfo.name().local();
        } else {
            return classInfo.name().local() + "Input";
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
                if (!scalarMap.containsKey(type.name())) {
                    LOG.error("No scalar mapping for " + type.name() + " with kind " + type.kind());
                }
                break;
            case CLASS:
                if (!scalarMap.containsKey(type.name())) {
                    ClassInfo classInfo = index.getClassByName(type.name());
                    if (classInfo != null) {
                        scanClass(direction, classInfo);
                    } else {
                        LOG.error("Not indexed class " + type.name() + " with kind " + type.kind());
                    }
                }
                break;
            default:
                LOG.error("What should we do with field type of " + type.name() + " with kind " + type.kind());
                break;
        }
    }

    private boolean isSetter(String methodName) {
        return methodName.length() > 3 && methodName.startsWith("set");
    }

    private boolean isGetter(String methodName) {
        return (methodName.length() > 3 && methodName.startsWith("get"))
                || (methodName.length() > 2 && methodName.startsWith("is"));
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
        GraphQLFieldDefinition.Builder builder = GraphQLFieldDefinition.newFieldDefinition();
        for (FieldInfo field : fields) {
            // Name (@JsonbProperty) TODO: What about our own annotation ?
            builder = builder.name(getNameFromField(field));
            // Description
            Optional<String> maybeFieldDescription = getDescription(field);
            if (maybeFieldDescription.isPresent()) {
                builder = builder.description(maybeFieldDescription.get());
            }
            // Type
            Type type = field.type();
            DotName fieldTypeName = type.name();
            if (fieldTypeName.equals(classInfo.name())) {
                // Myself
                builder = builder.type(GraphQLTypeReference.typeRef(name));
            } else {
                // Another type    
                builder = builder.type(toGraphQLOutputType(type));
            }
            fieldDefinitions.add(builder.build());
        }
        return fieldDefinitions;
    }

    private List<GraphQLInputObjectField> getGraphQLInputObjectField(ClassInfo classInfo, String name) {
        List<GraphQLInputObjectField> inputObjectFields = new ArrayList<>();
        // Fields (TODO: Look at methods rather ? Or both ?)
        List<FieldInfo> fields = classInfo.fields();
        GraphQLInputObjectField.Builder builder = GraphQLInputObjectField.newInputObjectField();
        for (FieldInfo field : fields) {
            // Name (@JsonbProperty) TODO: What about our own annotation ?
            builder = builder.name(getNameFromField(field));
            // Description
            Optional<String> maybeFieldDescription = getDescription(field);
            if (maybeFieldDescription.isPresent()) {
                builder = builder.description(maybeFieldDescription.get());
            }
            // Type
            Type type = field.type();
            DotName fieldTypeName = type.name();
            if (fieldTypeName.equals(classInfo.name())) {
                // Myself
                builder = builder.type(GraphQLTypeReference.typeRef(name));
            } else {
                // Another type    
                builder = builder.type(toGraphQLInputType(type));
            }
            inputObjectFields.add(builder.build());
        }
        return inputObjectFields;
    }

    private GraphQLOutputType toGraphQLOutputType(Type type) {
        DotName fieldTypeName = type.name();

        if (scalarMap.containsKey(fieldTypeName)) {
            // Scalar
            return scalarMap.get(fieldTypeName);
        } else if (enumMap.containsKey(fieldTypeName)) {
            // Enum  
            return enumMap.get(fieldTypeName);
        } else if (type.kind().equals(Type.Kind.ARRAY)) {
            // Array 
            Type typeInArray = type.asArrayType().component();
            return GraphQLList.list(toGraphQLOutputType(typeInArray));
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            // TODO: Check if there is more than one type in the Collection, throw an exception ?
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return GraphQLList.list(toGraphQLOutputType(typeInCollection));
        } else if (outputClassesWeCareAbout.containsKey(type.name())) {
            // Reference to some type
            GraphQLTypeReference graphQLTypeReference = outputClassesWeCareAbout.get(type.name()).getGraphQLTypeReference();
            return graphQLTypeReference;
        } else {
            // Maps ? Intefaces ? Generics ?
            throw new RuntimeException("Don't know what to do with " + type);
        }
    }

    private GraphQLInputType toGraphQLInputType(Type type) {
        DotName fieldTypeName = type.name();

        if (scalarMap.containsKey(fieldTypeName)) {
            // Scalar
            return scalarMap.get(fieldTypeName);
        } else if (enumMap.containsKey(fieldTypeName)) {
            // Enum  
            return enumMap.get(fieldTypeName);
        } else if (type.kind().equals(Type.Kind.ARRAY)) {
            // Array 
            Type typeInArray = type.asArrayType().component();
            return GraphQLList.list(toGraphQLInputType(typeInArray));
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // Collections
            // TODO: Check if there is more than one type in the Collection, throw an exception ?
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return GraphQLList.list(toGraphQLInputType(typeInCollection));
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

    private Optional<String> getDescription(AnnotationInstance descriptionAnnotation) {
        if (descriptionAnnotation != null) {
            AnnotationValue value = descriptionAnnotation.value();
            if (value != null) {
                return Optional.of(value.asString());
            }
        }
        return Optional.empty();
    }

    private Optional<String> getDescription(FieldInfo fieldInfo) {
        Optional<AnnotationInstance> maybeDescription = findAnnotationInstance(fieldInfo.annotations(),
                Annotations.DESCRIPTION);
        if (maybeDescription.isPresent()) {
            return getDescription(maybeDescription.get());
        }
        return Optional.empty();
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

    private String getNameFromField(FieldInfo fieldInfo) {
        Optional<AnnotationInstance> maybeJsonProperty = findAnnotationInstance(fieldInfo.annotations(),
                Annotations.JSONB_PROPERTY);
        if (maybeJsonProperty.isPresent()) {
            AnnotationInstance annotation = maybeJsonProperty.get();
            if (annotation != null && annotation.value() != null && !annotation.value().asString().isEmpty()) {
                return annotation.value().asString();
            }
        }
        return fieldInfo.name();
    }
}

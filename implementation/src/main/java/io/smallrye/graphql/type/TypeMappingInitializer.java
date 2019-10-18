package io.smallrye.graphql.type;

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
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.index.Classes;

/**
 * Create a Maps of all the types.
 * This looks at all POJOs annotated with @Type or @InputType
 * and then also all return type and arguments on @Queries and @Mutations.
 * 
 * It produces a few maps, that can be injected anywhere in the code:
 * - objectMap - contains all object types.
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
    private final Map<DotName, GraphQLObjectType> objectMap = new HashMap<>();

    @Produces
    private final Map<DotName, GraphQLEnumType> enumMap = new HashMap<>();

    private final Map<DotName, TypeHolder> classesWeCareAbout = new HashMap<>();
    private final Map<DotName, TypeHolder> enumsWeCareAbout = new HashMap<>();

    @PostConstruct
    void init() {

        // First traverse all the types and create referenced types
        scanClassLevelAnnotations(Annotations.TYPE);
        scanClassLevelAnnotations(Annotations.INPUTTYPE);
        scanMethodLevelAnnotations(Annotations.QUERY);
        scanMethodLevelAnnotations(Annotations.MUTATION);

        for (Map.Entry<DotName, TypeHolder> e : enumsWeCareAbout.entrySet()) {
            this.enumMap.put(e.getKey(), createEnumType(e.getValue()));
            LOG.debug("adding [" + e.getKey() + "] to the enums list");
        }

        for (Map.Entry<DotName, TypeHolder> e : classesWeCareAbout.entrySet()) {
            this.objectMap.put(e.getKey(), createObjectType(e.getValue()));
            LOG.debug("adding [" + e.getKey() + "] to the object list");
        }

    }

    private void scanClassLevelAnnotations(DotName annotationName) {
        List<AnnotationInstance> annotations = this.index.getAnnotations(annotationName);
        for (AnnotationInstance annotation : annotations) {

            if (annotation.target().kind().equals(AnnotationTarget.Kind.CLASS)) {
                ClassInfo classInfo = annotation.target().asClass();
                scanClass(classInfo);
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
                        scanType(methodInfo.returnType());
                    }
                    // arguments on getters and setter
                    List<Type> parameters = methodInfo.parameters();
                    for (Type parameter : parameters) {
                        scanType(parameter);
                    }
                    break;
            }
        }
    }

    private void scanClass(ClassInfo classInfo) {
        // TODO: Get name from Annotation (Can name be from InputType, Type or JSonbProperty ? Or somewhere else ?)
        String name = classInfo.name().local();

        if (Classes.isEnum(classInfo)) {
            if (!enumsWeCareAbout.containsKey(classInfo.name())) {
                enumsWeCareAbout.put(classInfo.name(), new TypeHolder(name, classInfo));
            }
        } else {
            if (!classesWeCareAbout.containsKey(classInfo.name())) {
                classesWeCareAbout.put(classInfo.name(), new TypeHolder(name, classInfo));

                // fields
                List<FieldInfo> fieldInfos = classInfo.fields();
                for (FieldInfo fieldInfo : fieldInfos) {
                    Type type = fieldInfo.type();
                    scanType(type);
                }

                // methods
                List<MethodInfo> methodInfos = classInfo.methods();
                for (MethodInfo methodInfo : methodInfos) {
                    String methodName = methodInfo.name();

                    // return types on getters and setters
                    if (isSetter(methodName) || isGetter(methodName)) {
                        // TODO: What if getting has void ?
                        if (!methodInfo.returnType().kind().equals(Type.Kind.VOID)) {
                            scanType(methodInfo.returnType());
                        }

                        // arguments on getters and setter
                        List<Type> parameters = methodInfo.parameters();
                        for (Type parameter : parameters) {
                            scanType(parameter);
                        }
                    }
                }
            }
        }
    }

    private void scanType(Type type) {
        switch (type.kind()) {
            case ARRAY:
                Type typeInArray = type.asArrayType().component();
                scanType(typeInArray);
                break;
            case PARAMETERIZED_TYPE:
                // TODO: Check if there is more than one type in the Collection, throw an exception ?
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                scanType(typeInCollection);
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
                        scanClass(classInfo);
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

    private GraphQLObjectType createObjectType(TypeHolder typeHolder) {
        String name = typeHolder.getNameInSchema();
        ClassInfo classInfo = typeHolder.getClassInfo();

        GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject();
        objectTypeBuilder = objectTypeBuilder.name(name);

        // Description
        Optional<String> maybeDescription = getDescription(classInfo);
        if (maybeDescription.isPresent()) {
            objectTypeBuilder = objectTypeBuilder.description(maybeDescription.get());
        }

        // Fields (TODO: Look at methods rather ? Or both ?)
        List<FieldInfo> fields = classInfo.fields();
        for (FieldInfo field : fields) {
            GraphQLFieldDefinition.Builder fieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition();
            // Name (@JsonbProperty) TODO: What about our own annotation ?
            fieldDefinitionBuilder = fieldDefinitionBuilder.name(getNameFromField(field));
            // Description
            Optional<String> maybeFieldDescription = getDescription(field);
            if (maybeFieldDescription.isPresent()) {
                fieldDefinitionBuilder = fieldDefinitionBuilder.description(maybeFieldDescription.get());
            }
            // Type
            Type type = field.type();
            DotName fieldTypeName = type.name();
            // Myself
            if (fieldTypeName.equals(classInfo.name())) {
                fieldDefinitionBuilder = fieldDefinitionBuilder.type(GraphQLTypeReference.typeRef(name));
                // Another type    
            } else {
                fieldDefinitionBuilder = fieldDefinitionBuilder.type(toGraphQLOutputType(type));
            }

            objectTypeBuilder.field(fieldDefinitionBuilder.build());
        }

        return objectTypeBuilder.build();
    }

    private GraphQLOutputType toGraphQLOutputType(Type type) {
        DotName fieldTypeName = type.name();
        // Scalar
        if (scalarMap.containsKey(fieldTypeName)) {
            return scalarMap.get(fieldTypeName);
            // Enum    
        } else if (enumMap.containsKey(fieldTypeName)) {
            return enumMap.get(fieldTypeName);
            // Array    
        } else if (type.kind().equals(Type.Kind.ARRAY)) {
            Type typeInArray = type.asArrayType().component();
            return GraphQLList.list(toGraphQLOutputType(typeInArray));
            // Collections
        } else if (type.kind().equals(Type.Kind.PARAMETERIZED_TYPE)) {
            // TODO: Check if there is more than one type in the Collection, throw an exception ?
            Type typeInCollection = type.asParameterizedType().arguments().get(0);
            return GraphQLList.list(toGraphQLOutputType(typeInCollection));
            // Reference to some type
        } else if (classesWeCareAbout.containsKey(type.name())) {
            GraphQLTypeReference graphQLTypeReference = classesWeCareAbout.get(type.name()).getGraphQLTypeReference();
            return graphQLTypeReference;
        } else {
            // Maps ? Intefaces ? Generics ?
            throw new RuntimeException("Don't know what to do with " + type);
        }
    }

    private Optional<String> getDescription(ClassInfo classInfo) {
        // See if there is a @Description Annotation on the class
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
        // See if there is a @Description Annotation on the field
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

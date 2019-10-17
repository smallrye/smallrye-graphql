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
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.index.Classes;

/**
 * Create a Maps of all the types
 * This looks at all POJOs annotated with @Type or @InputType,
 * and then also all return type and arguments on @Queries and @Mutations.
 * 
 * TODO: Generics ?
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

    // DONE:
    //  Enum
    //  Scalar

    // BUSE WITH:
    //  Object

    // TODO: 
    //  Interface
    //  Union
    //  InputObject

    @PostConstruct
    void init() {
        // Actual POJOS
        discoverType();
        discoverInputType();
        // Return type and input parameters
        discoverReturnTypeAndArguments(Annotations.QUERY);
        discoverReturnTypeAndArguments(Annotations.MUTATION);
    }

    private void discoverType() {
        LOG.debug("Finding all " + Annotations.TYPE + "...");
        List<AnnotationInstance> annotations = this.index.getAnnotations(Annotations.TYPE);

        for (AnnotationInstance annotation : annotations) {
            switch (annotation.target().kind()) {
                case CLASS:
                    // This is when we annotate with @Type related annotations. TODO: Add JsonB ?
                    ClassInfo classInfo = annotation.target().asClass();
                    discoveredType(classInfo);
                    break;
            }
        }
    }

    private void discoverInputType() {
        LOG.debug("Finding all " + Annotations.INPUTTYPE + "...");
        List<AnnotationInstance> annotations = this.index.getAnnotations(Annotations.INPUTTYPE);

        for (AnnotationInstance annotation : annotations) {
            switch (annotation.target().kind()) {
                case CLASS:
                    // This is when we annotate with @InputType related annotations. TODO: Add JsonB ?
                    ClassInfo classInfo = annotation.target().asClass();
                    discoveredType(classInfo);
                    break;
            }
        }
    }

    private void discoverReturnTypeAndArguments(DotName annotationName) {
        LOG.error("Finding all " + annotationName + "...");
        List<AnnotationInstance> annotations = this.index.getAnnotations(annotationName);

        for (AnnotationInstance annotation : annotations) {
            switch (annotation.target().kind()) {
                case CLASS:
                    // TODO: Do we allow Query on class level ?
                    break;
                case METHOD:
                    MethodInfo methodInfo = annotation.target().asMethod();
                    // Return types Queries
                    Type type = methodInfo.returnType();
                    discoverReturnOrInputObject(type);

                    // Arguments on Queries
                    List<Type> parameters = methodInfo.parameters();
                    for (Type t : parameters) {
                        discoverReturnOrInputObject(t);
                    }

                    break;
            }
        }
    }

    private void discoverReturnOrInputObject(Type type) {
        DotName typeName = type.name();

        if (notInScalarMap(typeName)) {
            switch (type.kind()) {
                case VOID:
                    // No type to find here...
                    break;
                case ARRAY:
                    ArrayType arrayType = type.asArrayType();
                    discoverReturnOrInputObject(arrayType.component());
                    break;
                case PARAMETERIZED_TYPE:
                    ParameterizedType parameterizedType = type.asParameterizedType();
                    List<Type> collectionType = parameterizedType.arguments();
                    for (Type t : collectionType) {
                        discoverReturnOrInputObject(t);
                    }
                    break;
                case CLASS:
                    if (notInEnumMap(typeName) && notInObjectMap(typeName)) {
                        ClassInfo classInfo = index.getClassByName(typeName);
                        if (classInfo != null) {
                            discoveredType(classInfo);
                        } else {
                            LOG.error("Not sure what to do with return type on Query [" + typeName + "]");
                        }
                    }
                    break;
                // TODO: check the kind (Interface Unions etc)    
                default:
                    LOG.warn("Not sure how to handle " + typeName + " of kind " + type.kind() + " . Ignoring");
                    break;
            }
        }
    }

    private void discoveredType(ClassInfo classInfo) {

        if (typeIsEnum(classInfo)) {
            createEnumType(classInfo);
        } else if (typeIsObject(classInfo)) {
            createObjectType(classInfo);
        } else {
            LOG.error("Don't know what to do with [" + classInfo + "]");
        }
    }

    // TODO: Test a more complex enum
    private void createEnumType(ClassInfo classInfo) {
        // TODO: Can name be from JSonbProperty ? Or somewhere else ?
        GraphQLEnumType.Builder builder = GraphQLEnumType.newEnum()
                .name(classInfo.name().local());

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

        LOG.warn("adding [" + classInfo.name() + "] of kind [" + classInfo.kind() + "] to the enums list");
        this.enumMap.put(classInfo.name(), builder.build());
    }

    private void createObjectType(ClassInfo classInfo) {
        GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject();
        // Name TODO: Get name from annotation ? @Input @InputType ?
        objectTypeBuilder = objectTypeBuilder.name(classInfo.name().local());

        // Description
        Optional<String> maybeDescription = getDescription(classInfo);
        if (maybeDescription.isPresent()) {
            objectTypeBuilder = objectTypeBuilder.description(maybeDescription.get());
        }

        // Fields (TODO: Look at methods rather ?)
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
            if (scalarMap.containsKey(fieldTypeName)) {
                fieldDefinitionBuilder = fieldDefinitionBuilder.type(scalarMap.get(fieldTypeName));
            } else if (enumMap.containsKey(fieldTypeName)) {
                fieldDefinitionBuilder = fieldDefinitionBuilder.type(enumMap.get(fieldTypeName));
            } else if (objectMap.containsKey(fieldTypeName)) {
                fieldDefinitionBuilder = fieldDefinitionBuilder.type(objectMap.get(fieldTypeName));
            } else {
                fieldDefinitionBuilder = fieldDefinitionBuilder.type(Scalars.GraphQLByte); // TODO: HERE !!
            }

            objectTypeBuilder.field(fieldDefinitionBuilder.build());
        }

        LOG.warn("adding [" + classInfo.name() + "] to the types list");
        this.objectMap.put(classInfo.name(), objectTypeBuilder.build());

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

    private boolean isSetter(String methodName) {
        return methodName.length() > 3 && methodName.startsWith("set");
    }

    private boolean isGetter(String methodName) {
        return (methodName.length() > 3 && methodName.startsWith("get"))
                || (methodName.length() > 2 && methodName.startsWith("is"));
    }

    private boolean typeIsObject(ClassInfo classInfo) {
        return classInfo.kind().equals(ClassInfo.Kind.CLASS) && notInObjectMap(classInfo.name());
    }

    private boolean typeIsEnum(ClassInfo classInfo) {
        return classInfo.kind().equals(ClassInfo.Kind.CLASS) && Classes.isEnum(classInfo) && notInEnumMap(classInfo.name());
    }

    private boolean notInEnumMap(DotName d) {
        return !enumMap.keySet().contains(d);
    }

    private boolean notInObjectMap(DotName d) {
        return !objectMap.keySet().contains(d);
    }

    private boolean notInScalarMap(DotName d) {
        return !scalarMap.keySet().contains(d);
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

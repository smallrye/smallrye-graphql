package io.smallrye.graphql.schema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.index.Classes;
import io.smallrye.graphql.inspector.MethodArgumentInspector;
import io.smallrye.graphql.scalar.GraphQLScalarTypeCreator;

/**
 * Creates the GraphQL Schema
 * TODO: Check that class is annotated with GraphQLApi ?
 * TODO: Check duplication with TypeMappingInitializer
 * TODO: Exceptions
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLSchemaInitializer {
    private static final Logger LOG = Logger.getLogger(GraphQLSchemaInitializer.class.getName());

    @Inject
    private Index index;

    @Inject
    private GraphQLScalarTypeCreator graphQLScalarTypeCreator;

    @Inject
    private Map<DotName, GraphQLInputObjectType> inputObjectMap;

    @Inject
    private Map<DotName, GraphQLObjectType> outputObjectMap;

    @Inject
    private Map<DotName, GraphQLEnumType> enumMap;

    @Inject
    private MethodArgumentInspector methodArgumentInspector;

    @Inject
    @ConfigProperty(name = "mp.graphql.queryRootDescription", defaultValue = "Query root")
    private String queryRootDescription;

    @Inject
    @ConfigProperty(name = "mp.graphql.mutationRootDescription", defaultValue = "Mutation root")
    private String mutationRootDescription;

    public GraphQLSchema createGraphQLSchema() {
        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
        LOG.error("We have " + inputObjectMap.size() + " input objects");
        LOG.error("We have " + outputObjectMap.size() + " output objects");
        LOG.error("We have " + enumMap.size() + " enums");
        LOG.error("We have " + graphQLScalarTypeCreator.size() + " scalars");

        Set<GraphQLType> additionalTypes = new HashSet<>();
        additionalTypes.addAll(enumMap.values());
        additionalTypes.addAll(outputObjectMap.values());
        additionalTypes.addAll(inputObjectMap.values());
        schemaBuilder.additionalTypes(additionalTypes);

        GraphQLObjectType allQueries = getAllQueries();
        GraphQLObjectType allMutations = getAllMutations();
        schemaBuilder.query(allQueries);
        schemaBuilder.mutation(allMutations);

        return schemaBuilder.build();
    }

    private GraphQLObjectType getAllQueries() {
        return createGraphQLObjectType(Annotations.QUERY, QUERY, queryRootDescription);
    }

    private GraphQLObjectType getAllMutations() {
        return createGraphQLObjectType(Annotations.MUTATION, MUTATION, mutationRootDescription);
    }

    private <T> GraphQLObjectType createGraphQLObjectType(DotName annotationToScan, String name, String description) {
        List<AnnotationInstance> graphQLAnnotations = this.index.getAnnotations(annotationToScan);

        GraphQLObjectType.Builder queryTypeBuilder = GraphQLObjectType.newObject().name(name).description(description);

        for (AnnotationInstance graphQLAnnotation : graphQLAnnotations) {
            switch (graphQLAnnotation.target().kind()) {
                // TODO: Are we allowing class level ?
                case METHOD:

                    MethodInfo methodInfo = graphQLAnnotation.target().asMethod();

                    // Annotations on this method
                    Map<DotName, AnnotationInstance> annotations = getAnnotationsForThisMethod(methodInfo);

                    Type returnType = methodInfo.returnType();

                    GraphQLFieldDefinition.Builder fieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition();
                    // Name
                    String n = getNameFromAnnotation(graphQLAnnotation);
                    fieldDefinitionBuilder = fieldDefinitionBuilder.name(n);
                    // Description
                    Optional<String> maybeDescription = getDescription(methodInfo);
                    if (maybeDescription.isPresent()) {
                        fieldDefinitionBuilder = fieldDefinitionBuilder.description(maybeDescription.get());
                    }
                    // Type (output)
                    fieldDefinitionBuilder = fieldDefinitionBuilder.type(toGraphQLOutputType(returnType, annotations));

                    // Arguments (input)
                    List<Type> parameters = methodInfo.parameters();

                    short cnt = 0;
                    for (Type parameter : parameters) {
                        fieldDefinitionBuilder.argument(toGraphQLArgument(methodInfo, cnt, parameter, annotations));
                        cnt++;
                    }

                    // TODO: Deprecate
                    // fieldDefinitionBuilder.deprecate(description)

                    // TODO: Directives ?
                    // fieldDefinitionBuilder.withDirectives(directives) // TODO ?

                    queryTypeBuilder = queryTypeBuilder.field(fieldDefinitionBuilder.build());
                    break;
                // TODO negative on other cases ?
            }
        }

        return queryTypeBuilder.build();
    }

    private GraphQLArgument toGraphQLArgument(MethodInfo methodInfo, short argCount, Type parameter,
            Map<DotName, AnnotationInstance> annotations) {
        String argName = getArgumentName(methodInfo, argCount);
        GraphQLInputType inputType = toGraphQLInputType(parameter, annotations);

        GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument();
        argumentBuilder = argumentBuilder.name(argName);
        argumentBuilder = argumentBuilder.type(inputType);
        Optional<AnnotationValue> maybeDefaultValue = methodArgumentInspector.getArgumentAnnotationValue(methodInfo, argCount,
                Annotations.DEFAULT_VALUE);
        if (maybeDefaultValue.isPresent()) {
            argumentBuilder = argumentBuilder.defaultValue(maybeDefaultValue.get().value());
        }

        return argumentBuilder.build();
    }

    private Map<DotName, AnnotationInstance> getAnnotationsForThisMethod(MethodInfo methodInfo) {

        Map<DotName, AnnotationInstance> annotationMap = new HashMap<>();

        for (AnnotationInstance annotationInstance : methodInfo.annotations()) {
            DotName name = annotationInstance.name();
            annotationMap.put(name, annotationInstance);
        }

        return annotationMap;
    }

    public String getArgumentName(MethodInfo methodInfo, short argCount) {
        Optional<AnnotationValue> argumentAnnotationValue = methodArgumentInspector.getArgumentAnnotationValue(methodInfo,
                argCount,
                Annotations.ARGUMENT);
        if (argumentAnnotationValue.isPresent())
            return String.valueOf(argumentAnnotationValue.get().asString());
        int nice = argCount + 1;
        return "arg" + nice;
    }

    private GraphQLOutputType toGraphQLOutputType(Type type, Map<DotName, AnnotationInstance> annotations) {
        // Type
        switch (type.kind()) {
            case VOID:
                LOG.warn("Ignoring void return"); // TODO: Throw an exception ?
                return getNoMappingScalarType(type);
            case ARRAY:
                Type typeInArray = type.asArrayType().component();
                return GraphQLList.list(toGraphQLOutputType(typeInArray, annotations));
            case PARAMETERIZED_TYPE:
                // TODO: Check if there is more than one type in the Collection, throw an exception ?
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return GraphQLList.list(toGraphQLOutputType(typeInCollection, annotations));
            case CLASS:
                Optional<GraphQLOutputType> maybeOutput = getGraphQLOutputType(type, annotations);
                if (maybeOutput.isPresent()) {
                    return maybeOutput.get();
                } else {
                    return getNoMappingScalarType(type);
                }
            case PRIMITIVE:
                Optional<GraphQLScalarType> maybeScalar = toGraphQLScalarType(type, annotations);
                if (maybeScalar.isPresent()) {
                    return maybeScalar.get();
                } else {
                    return getNoMappingScalarType(type);
                }
                // TODO: check the other kinds (Maps, Interface, Unions etc)    
            default:
                return getNoMappingScalarType(type);
        }
    }

    private GraphQLInputType toGraphQLInputType(Type type, Map<DotName, AnnotationInstance> annotations) {
        // Type
        switch (type.kind()) {
            case ARRAY:
                Type typeInArray = type.asArrayType().component();
                return GraphQLList.list(toGraphQLInputType(typeInArray, annotations));
            case PARAMETERIZED_TYPE:
                // TODO: Check if there is more than one type in the Collection, throw an exception ?
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return GraphQLList.list(toGraphQLInputType(typeInCollection, annotations));
            case CLASS:
                Optional<GraphQLInputType> maybeInput = getGraphQLInputType(type, annotations);
                if (maybeInput.isPresent()) {
                    return maybeInput.get();
                } else {
                    return getNoMappingScalarType(type);
                }
            case PRIMITIVE:
                Optional<GraphQLScalarType> maybeScalar = toGraphQLScalarType(type, annotations);
                if (maybeScalar.isPresent()) {
                    return maybeScalar.get();
                } else {
                    return getNoMappingScalarType(type);
                }
                // TODO: check the other kinds (Maps, Interface, Unions etc)    
            default:
                return getNoMappingScalarType(type);
        }
    }

    private GraphQLScalarType getNoMappingScalarType(Type type) {
        // TODO: How should this be handled ? Exception or default Object scalar ?
        LOG.warn("Can not get field type mapping for " + type.name() + " of kind " + type.kind()
                + " - default to String");
        return Scalars.GraphQLString;
    }

    private Optional<GraphQLOutputType> getGraphQLOutputType(Type type, Map<DotName, AnnotationInstance> annotations) {
        Optional<GraphQLScalarType> maybeScalar = toGraphQLScalarType(type, annotations);
        Optional<GraphQLEnumType> maybeEnum = toGraphQLEnumType(type);
        Optional<GraphQLObjectType> maybeObject = toGraphQLObjectType(type);

        if (maybeScalar.isPresent()) {
            return Optional.of(maybeScalar.get());
        } else if (maybeEnum.isPresent()) {
            return Optional.of(maybeEnum.get());
        } else if (maybeObject.isPresent()) {
            return Optional.of(maybeObject.get());
        }
        return Optional.empty();
    }

    private Optional<GraphQLInputType> getGraphQLInputType(Type type, Map<DotName, AnnotationInstance> annotations) {
        Optional<GraphQLScalarType> maybeScalar = toGraphQLScalarType(type, annotations);
        Optional<GraphQLEnumType> maybeEnum = toGraphQLEnumType(type);
        Optional<GraphQLInputObjectType> maybeObject = toGraphQLInputObjectType(type);

        if (maybeScalar.isPresent()) {
            return Optional.of(maybeScalar.get());
        } else if (maybeEnum.isPresent()) {
            return Optional.of(maybeEnum.get());
        } else if (maybeObject.isPresent()) {
            return Optional.of(maybeObject.get());
        }
        return Optional.empty();
    }

    private Optional<GraphQLScalarType> toGraphQLScalarType(Type type, Map<DotName, AnnotationInstance> annotations) {
        // ID - First check if the type is set with an annotation like @Id 
        // TODO: What if this annotation is not on a scalar ??
        if (hasIdAnnotation(type)) {
            return Optional.of(Scalars.GraphQLID);
        }

        // Scalar - Else get the type from the mappings
        DotName dotName = type.name();
        if (graphQLScalarTypeCreator.isScalarType(dotName)) {
            return Optional.of(graphQLScalarTypeCreator.getGraphQLScalarType(dotName, annotations));
        }

        return Optional.empty();
    }

    private Optional<GraphQLEnumType> toGraphQLEnumType(Type type) {
        DotName dotName = type.name();
        if (enumMap.containsKey(dotName)) {
            ClassInfo clazz = index.getClassByName(dotName);

            if (Classes.isEnum(clazz)) {
                GraphQLEnumType enumtype = enumMap.get(dotName);
                return Optional.of(enumtype);
            }
        }
        return Optional.empty();
    }

    private Optional<GraphQLObjectType> toGraphQLObjectType(Type type) {
        DotName dotName = type.name();
        if (outputObjectMap.containsKey(dotName)) {
            ClassInfo clazz = index.getClassByName(dotName);
            if (!Classes.isEnum(clazz)) {
                GraphQLObjectType objectType = outputObjectMap.get(dotName);
                return Optional.of(objectType);
            }
        }
        return Optional.empty();
    }

    private Optional<GraphQLInputObjectType> toGraphQLInputObjectType(Type type) {
        DotName dotName = type.name();
        if (inputObjectMap.containsKey(dotName)) {
            ClassInfo clazz = index.getClassByName(dotName);
            if (!Classes.isEnum(clazz)) {
                GraphQLInputObjectType objectType = inputObjectMap.get(dotName);
                return Optional.of(objectType);
            }
        }

        return Optional.empty();
    }

    private boolean hasIdAnnotation(Type type) {
        // See if there is a @Id Annotation on return type
        List<AnnotationInstance> annotations = type.annotations();
        for (AnnotationInstance annotation : annotations) {
            if (annotation.name().equals(Annotations.ID)) {
                return true;
            }
        }
        return false;
    }

    private String getNameFromAnnotation(AnnotationInstance annotation) {
        if (annotation.value() == null || annotation.value().asString().isEmpty()) {
            return annotation.target().asMethod().name();
        } else {
            return annotation.value().asString();
        }
    }

    private Optional<String> getDescription(MethodInfo methodInfo) {
        // See if there is a @Description Annotation on the method
        AnnotationInstance descriptionAnnotation = methodInfo.annotation(Annotations.DESCRIPTION);
        return getDescription(descriptionAnnotation);
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

    private static final String QUERY = "Query";
    private static final String MUTATION = "Mutation";

}

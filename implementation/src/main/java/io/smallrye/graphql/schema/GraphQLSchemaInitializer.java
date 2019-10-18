package io.smallrye.graphql.schema;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.idl.SchemaPrinter;
import io.smallrye.graphql.index.Annotations;
import io.smallrye.graphql.index.Classes;

/**
 * Creates the GraphQL Schema
 * TODO: Make schema available for injection
 * TODO: Check that class is annotated with GraphQLApi ?
 * TODO: Default value ? Can that be on the schema ?
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLSchemaInitializer {
    private static final Logger LOG = Logger.getLogger(GraphQLSchemaInitializer.class.getName());

    @Inject
    private Index index;

    @Inject
    private Map<DotName, GraphQLScalarType> scalarMap;

    @Inject
    private Map<DotName, GraphQLObjectType> objectMap;

    @Inject
    private Map<DotName, GraphQLEnumType> enumMap;

    @Produces
    private GraphQLSchema graphQLSchema;

    public void createGraphQLSchema() {
        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();
        LOG.error("We have " + objectMap.size() + " objects");
        LOG.error("We have " + enumMap.size() + " enums");
        LOG.error("We have " + scalarMap.size() + " scalars");

        Set<GraphQLType> additionalTypes = new HashSet<>();
        additionalTypes.addAll(enumMap.values());
        additionalTypes.addAll(objectMap.values());
        schemaBuilder.additionalTypes(additionalTypes);

        GraphQLObjectType allQueries = getAllQueries();
        GraphQLObjectType allMutations = getAllMutations();
        schemaBuilder.query(allQueries);
        schemaBuilder.mutation(allMutations);

        this.graphQLSchema = schemaBuilder.build();
        printGraphQLSchema();
    }

    private GraphQLObjectType getAllQueries() {
        return createGraphQLObjectType(Annotations.QUERY, QUERY, "Query root"); // TODO: Make description configurable ?
    }

    private GraphQLObjectType getAllMutations() {
        return createGraphQLObjectType(Annotations.MUTATION, MUTATION, "Mutation root"); // TODO: Make description configurable ?
    }

    private <T> GraphQLObjectType createGraphQLObjectType(DotName annotationToScan, String name, String description) {
        List<AnnotationInstance> graphQLAnnotations = this.index.getAnnotations(annotationToScan);

        GraphQLObjectType.Builder queryTypeBuilder = GraphQLObjectType.newObject().name(name).description(description);

        for (AnnotationInstance graphQLAnnotation : graphQLAnnotations) {
            switch (graphQLAnnotation.target().kind()) {

                case METHOD:

                    MethodInfo methodInfo = graphQLAnnotation.target().asMethod();

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
                    // Type
                    fieldDefinitionBuilder = fieldDefinitionBuilder.type(toGraphQLOutputType(returnType));

                    // TODO: Arguments

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

    private GraphQLOutputType toGraphQLOutputType(Type type) {
        // Type
        switch (type.kind()) {
            case VOID:
                LOG.warn("Ignoring void return"); // TODO: Throw an exception ?
                return getNoMappingScalarType(type);
            case ARRAY:
                Type typeInArray = type.asArrayType().component();
                return GraphQLList.list(toGraphQLOutputType(typeInArray));
            case PARAMETERIZED_TYPE:
                // TODO: Check if there is more than one type in the Collection, throw an exception ?
                Type typeInCollection = type.asParameterizedType().arguments().get(0);
                return GraphQLList.list(toGraphQLOutputType(typeInCollection));
            case CLASS:
                Optional<GraphQLOutputType> maybeOutput = getGraphQLOutputType(type);
                if (maybeOutput.isPresent()) {
                    return maybeOutput.get();
                } else {
                    return getNoMappingScalarType(type);
                }
            case PRIMITIVE:
                Optional<GraphQLScalarType> maybeScalar = toGraphQLScalarType(type);
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

    private Optional<GraphQLOutputType> getGraphQLOutputType(Type type) {
        Optional<GraphQLScalarType> maybeScalar = toGraphQLScalarType(type);
        Optional<GraphQLEnumType> maybeEnum = toGraphQLEnumType(type);
        Optional<GraphQLObjectType> maybeObject = toGraphqlObjectType(type);

        if (maybeScalar.isPresent()) {
            return Optional.of(maybeScalar.get());
        } else if (maybeEnum.isPresent()) {
            return Optional.of(maybeEnum.get());
        } else if (maybeObject.isPresent()) {
            return Optional.of(maybeObject.get());
        }
        return Optional.empty();
    }

    private Optional<GraphQLScalarType> toGraphQLScalarType(Type type) {
        // ID - First check if the type is set with an annotation like @Id 
        // TODO: What if this annotation is not on a scalar ??
        if (hasIdAnnotation(type)) {
            //LOG.warn("... id");
            return Optional.of(Scalars.GraphQLID);
        }

        // Scalar - Else get the type from the mappings
        DotName dotName = type.name();
        if (scalarMap.containsKey(dotName)) {
            //LOG.warn("... scalar");
            return Optional.of(scalarMap.get(dotName));
        }
        return Optional.empty();
    }

    private Optional<GraphQLEnumType> toGraphQLEnumType(Type type) {
        DotName dotName = type.name();
        if (enumMap.containsKey(dotName)) {
            ClassInfo clazz = index.getClassByName(dotName);
            if (Classes.isEnum(clazz)) {
                //LOG.warn("... enum");
                GraphQLEnumType enumtype = enumMap.get(dotName);
                // Do stuff ?
                return Optional.of(enumtype);
            }
        }
        return Optional.empty();
    }

    private Optional<GraphQLObjectType> toGraphqlObjectType(Type type) {
        DotName dotName = type.name();
        if (objectMap.containsKey(dotName)) {
            ClassInfo clazz = index.getClassByName(dotName);
            if (!Classes.isEnum(clazz)) {
                //LOG.warn("... object");
                GraphQLObjectType objectType = objectMap.get(dotName);
                // Do stuff ?
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

    // -----------------
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

    private void printGraphQLSchema() {
        SchemaPrinter schemaPrinter = new SchemaPrinter();//TODO: ? SchemaPrinter.Options.defaultOptions().includeSchemaDefintion(true));
        String schemaString = schemaPrinter.print(this.graphQLSchema);
        LOG.error("\n\n" + schemaString);
    }

    private static final String QUERY = "Query";
    private static final String MUTATION = "Mutation";

}

package io.smallrye.graphql.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.bootstrap.datafetcher.CdiReflectionDataFetcher;
import io.smallrye.graphql.bootstrap.typeresolver.InterfaceResolver;
import io.smallrye.graphql.schema.model.Complex;
import io.smallrye.graphql.schema.model.Enum;
import io.smallrye.graphql.schema.model.Method;
import io.smallrye.graphql.schema.model.Parameter;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Return;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Bootstrap MicroProfile GraphQL
 * This create a graphql-java model from the smallrye model
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Bootstrap {
    private static final Logger LOG = Logger.getLogger(Bootstrap.class.getName());

    private final Schema schema;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

    private final Map<String, GraphQLEnumType> enumMap = new HashMap<>();
    private final Map<String, GraphQLInterfaceType> interfaceMap = new HashMap<>();
    private final Map<String, GraphQLInputObjectType> inputMap = new HashMap<>();
    private final Map<String, GraphQLObjectType> typeMap = new HashMap<>();

    public static GraphQLSchema bootstrap(Schema schema) {
        Bootstrap graphQLBootstrap = new Bootstrap(schema);
        return graphQLBootstrap.generateGraphQLSchema();
    }

    private Bootstrap(Schema schema) {
        this.schema = schema;
    }

    private GraphQLSchema generateGraphQLSchema() {
        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();

        createGraphQLEnumTypes();
        createGraphQLInterfaceTypes();
        createGraphQLObjectTypes();
        createGraphQLInputObjectTypes();

        addQueries(schemaBuilder);
        addMutations(schemaBuilder);

        schemaBuilder.additionalTypes(new HashSet<>(enumMap.values()));
        schemaBuilder.additionalTypes(new HashSet<>(interfaceMap.values()));
        schemaBuilder.additionalTypes(new HashSet<>(typeMap.values()));
        schemaBuilder.additionalTypes(new HashSet<>(inputMap.values()));

        schemaBuilder = schemaBuilder.codeRegistry(codeRegistryBuilder.build());

        return schemaBuilder.build();
    }

    // Create all enums and map them
    private void createGraphQLEnumTypes() {
        for (Enum enumType : schema.getEnums().values()) {
            createGraphQLEnumType(enumType);
        }
    }

    private void createGraphQLEnumType(Enum enumType) {
        GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum()
                .name(enumType.getName())
                .description(enumType.getDescription());
        // Values
        for (String value : enumType.getValues()) {
            enumBuilder = enumBuilder.value(value);
        }
        GraphQLEnumType graphQLEnumType = enumBuilder.build();
        enumMap.put(enumType.getClassName(), graphQLEnumType);
    }

    private void createGraphQLInterfaceTypes() {
        for (Complex interfaceType : schema.getInterfaces().values()) {
            createGraphQLInterfaceType(interfaceType);
        }
    }

    private void createGraphQLInterfaceType(Complex interfaceType) {
        GraphQLInterfaceType.Builder interfaceTypeBuilder = GraphQLInterfaceType.newInterface()
                .name(interfaceType.getName())
                .description(interfaceType.getDescription());

        // Fields TODO: Add datafetchers
        if (interfaceType.hasMethods()) {
            Set<Method> methods = interfaceType.getMethods();
            interfaceTypeBuilder = interfaceTypeBuilder.fields(createGraphQLFieldDefinitions(methods));
        }

        // Source Fields TODO: Add datafetchers
        if (interfaceType.hasSources()) {
            Set<Method> sources = interfaceType.getSources();
            interfaceTypeBuilder = interfaceTypeBuilder.fields(createGraphQLFieldDefinitions(sources));
        }

        GraphQLInterfaceType graphQLInterfaceType = interfaceTypeBuilder.build();
        // To resolve the concrete class
        codeRegistryBuilder.typeResolver(graphQLInterfaceType,
                new InterfaceResolver(interfaceType.getClassName()));

        interfaceMap.put(interfaceType.getClassName(), graphQLInterfaceType);
    }

    private void createGraphQLInputObjectTypes() {
        for (Complex inputType : schema.getInputs().values()) {
            createGraphQLInputObjectType(inputType);
        }
    }

    private void createGraphQLInputObjectType(Complex inputType) {
        GraphQLInputObjectType.Builder inputObjectTypeBuilder = GraphQLInputObjectType.newInputObject()
                .name(inputType.getName())
                .description(inputType.getDescription());

        // Fields TODO: Add datafetchers
        if (inputType.hasMethods()) {
            Set<Method> methods = inputType.getMethods();
            inputObjectTypeBuilder = inputObjectTypeBuilder.fields(createGraphQLInputObjectFields(methods));
        }

        // Source Fields TODO: Add datafetchers
        if (inputType.hasSources()) {
            Set<Method> sources = inputType.getSources();
            inputObjectTypeBuilder = inputObjectTypeBuilder.fields(createGraphQLInputObjectFields(sources));
        }

        GraphQLInputObjectType graphQLInputObjectType = inputObjectTypeBuilder.build();
        inputMap.put(inputType.getClassName(), graphQLInputObjectType);
    }

    private void createGraphQLObjectTypes() {
        for (Complex type : schema.getTypes().values()) {
            createGraphQLObjectType(type);
        }
    }

    private void createGraphQLObjectType(Complex type) {
        GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject()
                .name(type.getName())
                .description(type.getDescription());

        // Fields TODO: Add datafetchers
        if (type.hasMethods()) {
            Set<Method> methods = type.getMethods();
            objectTypeBuilder = objectTypeBuilder.fields(createGraphQLFieldDefinitions(methods));
        }

        // Source Fields TODO: Add datafetchers
        if (type.hasSources()) {
            Set<Method> sources = type.getSources();
            objectTypeBuilder = objectTypeBuilder.fields(createGraphQLFieldDefinitions(sources));
        }

        // Interfaces
        if (type.hasInterfaces()) {
            Set<Reference> interfaces = type.getInterfaces();
            for (Reference i : interfaces) {
                GraphQLInterfaceType graphQLInterfaceType = interfaceMap.get(i.getClassName());
                objectTypeBuilder = objectTypeBuilder.withInterface(graphQLInterfaceType);
            }
        }

        GraphQLObjectType graphQLObjectType = objectTypeBuilder.build();
        typeMap.put(type.getClassName(), graphQLObjectType);
    }

    private void addQueries(GraphQLSchema.Builder schemaBuilder) {

        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
                .name(QUERY)
                .description("Query root");

        Set<Complex> queries = schema.getQueries();
        for (Complex queryDefinition : queries) {
            Set<Method> methods = queryDefinition.getMethods();
            for (Method method : methods) {
                GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinition(method);
                queryBuilder = queryBuilder.field(graphQLFieldDefinition);
                CdiReflectionDataFetcher datafetcher = new CdiReflectionDataFetcher(queryDefinition.getClassName());
                codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(QUERY,
                        graphQLFieldDefinition.getName()), datafetcher);
            }

        }

        GraphQLObjectType query = queryBuilder.build();
        if (query.getFieldDefinitions() != null && !query.getFieldDefinitions().isEmpty()) {
            schemaBuilder.query(query);
        }
    }

    private void addMutations(GraphQLSchema.Builder schemaBuilder) {

        GraphQLObjectType.Builder mutationBuilder = GraphQLObjectType.newObject()
                .name(MUTATION)
                .description("Mutation root");

        Set<Complex> mutations = schema.getMutations();
        for (Complex mutationDefinition : mutations) {

            Set<Method> methods = mutationDefinition.getMethods();
            for (Method method : methods) {

                GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinition(method);
                mutationBuilder = mutationBuilder.field(graphQLFieldDefinition);

                CdiReflectionDataFetcher datafetcher = new CdiReflectionDataFetcher(mutationDefinition.getClassName());

                codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(MUTATION,
                        graphQLFieldDefinition.getName()), datafetcher);

            }
        }

        GraphQLObjectType mutation = mutationBuilder.build();
        if (mutation.getFieldDefinitions() != null && !mutation.getFieldDefinitions().isEmpty()) {
            schemaBuilder.mutation(mutation);
        }
    }

    private List<GraphQLFieldDefinition> createGraphQLFieldDefinitions(Set<Method> methods) {
        List<GraphQLFieldDefinition> graphQLFieldDefinitions = new ArrayList<>();

        for (Method method : methods) {
            graphQLFieldDefinitions.add(createGraphQLFieldDefinition(method));
        }
        return graphQLFieldDefinitions;
    }

    private GraphQLFieldDefinition createGraphQLFieldDefinition(Method method) {
        // Fields
        GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name(method.getName())
                .description(method.getDescription());

        // Type (output)
        Return returnObject = method.getReturn();
        fieldBuilder = fieldBuilder.type(createGraphQLOutputType(returnObject));

        // Arguments (queries and mutations)
        if (method.hasParameters()) {
            List<Parameter> parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                fieldBuilder = fieldBuilder.argument(createGraphQLArgument(parameter));
            }
        }

        return fieldBuilder.build();
    }

    private List<GraphQLInputObjectField> createGraphQLInputObjectFields(Set<Method> methods) {
        List<GraphQLInputObjectField> graphQLInputObjectFields = new ArrayList<>();
        for (Method method : methods) {
            graphQLInputObjectFields.add(createGraphQLInputObjectField(method));
        }
        return graphQLInputObjectFields;
    }

    private GraphQLInputObjectField createGraphQLInputObjectField(Method method) {
        GraphQLInputObjectField.Builder inputFieldBuilder = GraphQLInputObjectField.newInputObjectField()
                .name(method.getName())
                .description(method.getDescription());

        // Type
        Return returnObject = method.getReturn();
        inputFieldBuilder = inputFieldBuilder.type(createGraphQLInputType(returnObject));

        // Default value (on method)
        inputFieldBuilder = inputFieldBuilder.defaultValue(returnObject.getDefaultValue());

        GraphQLInputObjectField graphQLInputObjectField = inputFieldBuilder.build();

        //        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(name, fieldName),
        //                new AnnotatedPropertyDataFetcher(field.name(), field.type(), annotations));

        return graphQLInputObjectField;

        // Name mapping for input transformation
        //        if (!field.name().equals(fieldName)) {
        //            customFieldNameMapping.put(field.name(), fieldName);
        //        }
        //        // Other annotation for other transformation
        //        if (annotations.hasGraphQLFormatingAnnotations()) {
        //            Optional<String> description = descriptionHelper.getDescriptionForField(annotationsForThisArgument,
        //                    field.type());
        //            Argument a = new Argument(fieldName, description.orElse(null), field.type(), annotations);// TODO: Should this not be annotationsForThisArgument
        //
        //            fieldAnnotationsMapping.put(fieldName, a);
        //        }
    }

    private GraphQLInputType createGraphQLInputType(Return returnObject) {

        GraphQLInputType graphQLInputType = (GraphQLInputType) createGraphQLInputType(returnObject.getReturnType());

        // Collection
        if (returnObject.isCollection()) {
            // Mandatory in the collection
            if (returnObject.isMandatoryInCollection()) {
                graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
            }
            // Collection depth
            int depth = returnObject.getCollectionDepth();
            for (int i = 0; i < depth; i++) {
                graphQLInputType = GraphQLList.list(graphQLInputType);
            }
        }

        // Mandatory
        if (returnObject.isMandatory()) {
            graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
        }

        return graphQLInputType;
    }

    private GraphQLOutputType createGraphQLOutputType(Return returnObject) {
        GraphQLOutputType graphQLOutputType = (GraphQLOutputType) createGraphQLOutputType(returnObject.getReturnType());

        // Collection
        if (returnObject.isCollection()) {
            // Mandatory in the collection
            if (returnObject.isMandatoryInCollection()) {
                graphQLOutputType = GraphQLNonNull.nonNull(graphQLOutputType);
            }
            // Collection depth
            int depth = returnObject.getCollectionDepth();
            for (int i = 0; i < depth; i++) {
                graphQLOutputType = GraphQLList.list(graphQLOutputType);
            }
        }
        // Mandatory
        if (returnObject.isMandatory()) {
            graphQLOutputType = GraphQLNonNull.nonNull(graphQLOutputType);
        }
        return graphQLOutputType;
    }

    private GraphQLOutputType createGraphQLOutputType(Reference reference) {
        ReferenceType type = reference.getType();
        String className = reference.getClassName();
        String name = reference.getName();
        switch (type) {
            case SCALAR:
                return ScalarHolder.getGraphQLScalarType(className);
            case ENUM:
                return enumMap.get(className);
            case INPUT:
                return Scalars.GraphQLString;
            default:
                return GraphQLTypeReference.typeRef(name);
        }
    }

    private GraphQLInputType createGraphQLInputType(Reference reference) {
        ReferenceType type = reference.getType();
        String className = reference.getClassName();
        String name = reference.getName();
        switch (type) {
            case SCALAR:
                return ScalarHolder.getGraphQLScalarType(className);
            case ENUM:
                return enumMap.get(className);
            case TYPE:
                return Scalars.GraphQLID;
            default:
                return GraphQLTypeReference.typeRef(name);
        }
    }

    private GraphQLArgument createGraphQLArgument(Parameter parameter) {
        GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument()
                .name(parameter.getName())
                .description(parameter.getDescription())
                .defaultValue(parameter.getDefaultValue());

        GraphQLInputType createGraphQLType = createGraphQLInputType(parameter.getParameterType());

        argumentBuilder = argumentBuilder.type(createGraphQLType);

        return argumentBuilder.build();

    }

    private static final String QUERY = "Query";
    private static final String MUTATION = "Mutation";

}

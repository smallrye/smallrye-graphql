package io.smallrye.graphql.bootstrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

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
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import io.smallrye.graphql.execution.datafetcher.PropertyDataFetcher;
import io.smallrye.graphql.execution.datafetcher.ReflectionDataFetcher;
import io.smallrye.graphql.execution.resolver.InterfaceOutputRegistry;
import io.smallrye.graphql.execution.resolver.InterfaceResolver;
import io.smallrye.graphql.json.JsonInputRegistry;
import io.smallrye.graphql.scalar.GraphQLScalarTypes;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.Array;
import io.smallrye.graphql.schema.model.EnumType;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.InputType;
import io.smallrye.graphql.schema.model.InterfaceType;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.schema.model.Type;

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

    private final Map<String, GraphQLScalarType> scalarMap = GraphQLScalarTypes.getScalarMap();
    private final Map<String, GraphQLEnumType> enumMap = new HashMap<>();
    private final Map<String, GraphQLInterfaceType> interfaceMap = new HashMap<>();
    private final Map<String, GraphQLInputObjectType> inputMap = new HashMap<>();
    private final Map<String, GraphQLObjectType> typeMap = new HashMap<>();

    public static GraphQLSchema bootstrap(Schema schema) {
        if (schema != null) {
            Bootstrap graphQLBootstrap = new Bootstrap(schema);
            return graphQLBootstrap.generateGraphQLSchema();
        } else {
            LOG.warn("Schema is null, not bootstrapping SmallRye GraphQL");
            return null;
        }
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

    private void addQueries(GraphQLSchema.Builder schemaBuilder) {

        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
                .name(QUERY)
                .description("Query root");

        if (schema.hasQueries()) {
            Set<Operation> queries = schema.getQueries();
            for (Operation queryOperation : queries) {
                GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinitionFromOperation(QUERY,
                        queryOperation);
                queryBuilder = queryBuilder.field(graphQLFieldDefinition);
            }

            GraphQLObjectType query = queryBuilder.build();
            if (query.getFieldDefinitions() != null && !query.getFieldDefinitions().isEmpty()) {
                schemaBuilder.query(query);
            }
        }
    }

    private void addMutations(GraphQLSchema.Builder schemaBuilder) {

        GraphQLObjectType.Builder mutationBuilder = GraphQLObjectType.newObject()
                .name(MUTATION)
                .description("Mutation root");

        if (schema.hasMutations()) {
            Set<Operation> mutations = schema.getMutations();
            for (Operation mutationOperation : mutations) {
                GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinitionFromOperation(MUTATION,
                        mutationOperation);
                mutationBuilder = mutationBuilder.field(graphQLFieldDefinition);
            }

            GraphQLObjectType mutation = mutationBuilder.build();
            if (mutation.getFieldDefinitions() != null && !mutation.getFieldDefinitions().isEmpty()) {
                schemaBuilder.mutation(mutation);
            }
        }
    }

    // Create all enums and map them
    private void createGraphQLEnumTypes() {
        if (schema.hasEnums()) {
            for (EnumType enumType : schema.getEnums().values()) {
                createGraphQLEnumType(enumType);
            }
        }
    }

    private void createGraphQLEnumType(EnumType enumType) {
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
        if (schema.hasInterfaces()) {
            for (InterfaceType interfaceType : schema.getInterfaces().values()) {
                createGraphQLInterfaceType(interfaceType);
            }
        }
    }

    private void createGraphQLInterfaceType(InterfaceType interfaceType) {
        GraphQLInterfaceType.Builder interfaceTypeBuilder = GraphQLInterfaceType.newInterface()
                .name(interfaceType.getName())
                .description(interfaceType.getDescription());

        // Fields 
        if (interfaceType.hasFields()) {
            interfaceTypeBuilder = interfaceTypeBuilder
                    .fields(createGraphQLFieldDefinitionsFromFields(interfaceType.getName(), interfaceType.getFields()));
        }

        GraphQLInterfaceType graphQLInterfaceType = interfaceTypeBuilder.build();
        // To resolve the concrete class
        codeRegistryBuilder.typeResolver(graphQLInterfaceType,
                new InterfaceResolver(interfaceType));

        interfaceMap.put(interfaceType.getClassName(), graphQLInterfaceType);
    }

    private void createGraphQLInputObjectTypes() {
        if (schema.hasInputs()) {
            for (InputType inputType : schema.getInputs().values()) {
                createGraphQLInputObjectType(inputType);
            }
        }
    }

    private void createGraphQLInputObjectType(InputType inputType) {
        GraphQLInputObjectType.Builder inputObjectTypeBuilder = GraphQLInputObjectType.newInputObject()
                .name(inputType.getName())
                .description(inputType.getDescription());

        // Fields
        if (inputType.hasFields()) {
            inputObjectTypeBuilder = inputObjectTypeBuilder
                    .fields(createGraphQLInputObjectFieldsFromFields(inputType.getFields()));
            // Register this input for posible JsonB usage 
            JsonInputRegistry.register(inputType);
        }

        GraphQLInputObjectType graphQLInputObjectType = inputObjectTypeBuilder.build();
        inputMap.put(inputType.getClassName(), graphQLInputObjectType);
    }

    private void createGraphQLObjectTypes() {
        if (schema.hasTypes()) {
            for (Type type : schema.getTypes().values()) {
                createGraphQLObjectType(type);
            }
        }
    }

    private void createGraphQLObjectType(Type type) {
        GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject()
                .name(type.getName())
                .description(type.getDescription());

        // Fields
        if (type.hasFields()) {
            objectTypeBuilder = objectTypeBuilder
                    .fields(createGraphQLFieldDefinitionsFromFields(type.getName(), type.getFields()));
        }

        // Operations
        if (type.hasOperations()) {
            for (Operation operation : type.getOperations()) {
                GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinitionFromOperation(type.getName(),
                        operation);
                objectTypeBuilder = objectTypeBuilder.field(graphQLFieldDefinition);
            }
        }

        // Interfaces
        if (type.hasInterfaces()) {
            Set<Reference> interfaces = type.getInterfaces();
            for (Reference i : interfaces) {
                if (interfaceMap.containsKey(i.getClassName())) {
                    GraphQLInterfaceType graphQLInterfaceType = interfaceMap.get(i.getClassName());
                    objectTypeBuilder = objectTypeBuilder.withInterface(graphQLInterfaceType);
                }
            }
        }

        GraphQLObjectType graphQLObjectType = objectTypeBuilder.build();
        typeMap.put(type.getClassName(), graphQLObjectType);

        // Register this output for interface type resolving
        InterfaceOutputRegistry.register(type, graphQLObjectType);
    }

    private GraphQLFieldDefinition createGraphQLFieldDefinitionFromOperation(String operationTypeName, Operation operation) {
        // Fields
        GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name(operation.getName())
                .description(operation.getDescription());

        // Return field
        fieldBuilder = fieldBuilder.type(createGraphQLOutputType(operation));

        // Arguments
        if (operation.hasArguments()) {
            fieldBuilder = fieldBuilder.arguments(createGraphQLArguments(operation.getArguments()));
        }

        GraphQLFieldDefinition graphQLFieldDefinition = fieldBuilder.build();

        // DataFetcher
        ReflectionDataFetcher datafetcher = new ReflectionDataFetcher(operation);
        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(operationTypeName,
                graphQLFieldDefinition.getName()), datafetcher);

        return graphQLFieldDefinition;
    }

    private List<GraphQLFieldDefinition> createGraphQLFieldDefinitionsFromFields(String ownerName, Set<Field> fields) {
        List<GraphQLFieldDefinition> graphQLFieldDefinitions = new ArrayList<>();
        for (Field field : fields) {
            graphQLFieldDefinitions.add(createGraphQLFieldDefinitionFromField(ownerName, field));
        }
        return graphQLFieldDefinitions;
    }

    private GraphQLFieldDefinition createGraphQLFieldDefinitionFromField(String ownerName, Field field) {
        GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name(field.getName())
                .description(field.getDescription());

        // Type
        fieldBuilder = fieldBuilder.type(createGraphQLOutputType(field));

        GraphQLFieldDefinition graphQLFieldDefinition = fieldBuilder.build();

        // DataFetcher
        PropertyDataFetcher datafetcher = new PropertyDataFetcher(field);
        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(ownerName,
                graphQLFieldDefinition.getName()), datafetcher);

        return graphQLFieldDefinition;
    }

    private List<GraphQLInputObjectField> createGraphQLInputObjectFieldsFromFields(Set<Field> fields) {
        List<GraphQLInputObjectField> graphQLInputObjectFields = new ArrayList<>();
        for (Field field : fields) {
            graphQLInputObjectFields.add(createGraphQLInputObjectFieldFromField(field));
        }
        return graphQLInputObjectFields;
    }

    private GraphQLInputObjectField createGraphQLInputObjectFieldFromField(Field field) {
        GraphQLInputObjectField.Builder inputFieldBuilder = GraphQLInputObjectField.newInputObjectField()
                .name(field.getName())
                .description(field.getDescription());

        // Type
        inputFieldBuilder = inputFieldBuilder.type(createGraphQLInputType(field));

        // Default value (on method)
        inputFieldBuilder = inputFieldBuilder.defaultValue(field.getDefaultValue().orElse(null));

        GraphQLInputObjectField graphQLInputObjectField = inputFieldBuilder.build();

        return graphQLInputObjectField;
    }

    private GraphQLInputType createGraphQLInputType(Field field) {

        GraphQLInputType graphQLInputType = referenceGraphQLInputType(field);

        // Collection
        if (field.getArray().isPresent()) {
            Array array = field.getArray().get();
            // Mandatory in the collection
            if (array.isNotEmpty()) {
                graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
            }
            // Collection depth
            for (int i = 0; i < array.getDepth(); i++) {
                graphQLInputType = GraphQLList.list(graphQLInputType);
            }
        }

        // Mandatory
        if (field.isNotNull()) {
            graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
        }

        return graphQLInputType;
    }

    private GraphQLOutputType createGraphQLOutputType(Field field) {
        GraphQLOutputType graphQLOutputType = referenceGraphQLOutputType(field);

        // Collection
        if (field.getArray().isPresent()) {
            Array array = field.getArray().get();
            // Mandatory in the collection
            if (array.isNotEmpty()) {
                graphQLOutputType = GraphQLNonNull.nonNull(graphQLOutputType);
            }
            // Collection depth
            for (int i = 0; i < array.getDepth(); i++) {
                graphQLOutputType = GraphQLList.list(graphQLOutputType);
            }
        }

        // Mandatory
        if (field.isNotNull()) {
            graphQLOutputType = GraphQLNonNull.nonNull(graphQLOutputType);
        }

        return graphQLOutputType;
    }

    private GraphQLOutputType referenceGraphQLOutputType(Field field) {
        Reference reference = field.getReference();
        ReferenceType type = reference.getType();
        String className = reference.getClassName();
        String name = reference.getName();
        switch (type) {
            case SCALAR:
                return getCorrectScalarType(field, className);
            case ENUM:
                return enumMap.get(className);
            //case INTERFACE: ??        
            default:
                return GraphQLTypeReference.typeRef(name);
        }
    }

    private GraphQLInputType referenceGraphQLInputType(Field field) {
        Reference reference = field.getReference();
        ReferenceType type = reference.getType();
        String className = reference.getClassName();
        String name = reference.getName();
        switch (type) {
            case SCALAR:
                return getCorrectScalarType(field, className);
            case ENUM:
                return enumMap.get(className);
            //case INTERFACE: ??    
            default:
                return GraphQLTypeReference.typeRef(name);
        }
    }

    //&& !field.getTransformInfo().get().isValid()
    private GraphQLScalarType getCorrectScalarType(Field field, String className) {
        //        if (field.getTransformInfo().isPresent()
        //                && field.getTransformInfo().get().getType().equals(TransformInfo.Type.NUMBER)) { // Numbers that format should become Strings
        //            return (T) Scalars.GraphQLString; // then we change to String
        //        }
        return GraphQLScalarTypes.getScalarByName(field.getReference().getName());
    }

    private List<GraphQLArgument> createGraphQLArguments(List<Argument> arguments) {
        List<GraphQLArgument> graphQLArguments = new ArrayList<>();
        for (Argument argument : arguments) {
            if (!argument.isSourceArgument()) {
                graphQLArguments.add(createGraphQLArgument(argument));
            }
        }
        return graphQLArguments;
    }

    private GraphQLArgument createGraphQLArgument(Argument argument) {
        GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument()
                .name(argument.getName())
                .description(argument.getDescription())
                .defaultValue(argument.getDefaultValue().orElse(null));

        GraphQLInputType graphQLInputType = referenceGraphQLInputType(argument);

        // Collection
        if (argument.getArray().isPresent()) {
            Array array = argument.getArray().get();
            // Mandatory in the collection
            if (array.isNotEmpty()) {
                graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
            }
            // Collection depth
            for (int i = 0; i < array.getDepth(); i++) {
                graphQLInputType = GraphQLList.list(graphQLInputType);
            }
        }

        // Mandatory
        if (argument.isNotNull()) {
            graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
        }

        argumentBuilder = argumentBuilder.type(graphQLInputType);

        GraphQLArgument graphQLArgument = argumentBuilder.build();

        return graphQLArgument;

    }

    private static final String QUERY = "Query";
    private static final String MUTATION = "Mutation";
}

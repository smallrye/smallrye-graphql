package io.smallrye.graphql.bootstrap;

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
import io.smallrye.graphql.execution.datafetcher.ReflectionDataFetcher;
import io.smallrye.graphql.execution.resolver.InterfaceResolver;
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
import io.smallrye.graphql.x.ScalarHolder;

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
            for (Field field : interfaceType.getFields()) {
                // TODO: Add datafetchers
                interfaceTypeBuilder = interfaceTypeBuilder.field(createGraphQLFieldDefinition(field));
            }
        }

        GraphQLInterfaceType graphQLInterfaceType = interfaceTypeBuilder.build();
        // To resolve the concrete class
        codeRegistryBuilder.typeResolver(graphQLInterfaceType,
                new InterfaceResolver(interfaceType.getClassName()));

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
            for (Field field : inputType.getFields()) {
                // TODO: Add datafetchers
                inputObjectTypeBuilder = inputObjectTypeBuilder.field(createGraphQLInputObjectField(field));
            }
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
            for (Field field : type.getFields()) {
                // TODO: Add datafetchers
                objectTypeBuilder = objectTypeBuilder.field(createGraphQLFieldDefinition(field));
            }

        }

        // Operations
        if (type.hasOperations()) {
            for (Operation operation : type.getOperations()) {
                GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinition(operation);
                objectTypeBuilder = objectTypeBuilder.field(graphQLFieldDefinition);
                // TODO : add datafetchers
                //ReflectionDataFetcher datafetcher = new ReflectionDataFetcher(operation.getClassName(), method);
                //codeRegistryBuilder.dataFetcher(
                //        FieldCoordinates.coordinates(type.getName(), graphQLFieldDefinition.getName()), datafetcher);
            }
        }

        // TODO: Interfaces
        //        if (type.hasInterfaces()) {
        //            Set<Reference> interfaces = type.getInterfaces();
        //            for (Reference i : interfaces) {
        //                GraphQLInterfaceType graphQLInterfaceType = interfaceMap.get(i.getClassName());
        //                objectTypeBuilder = objectTypeBuilder.withInterface(graphQLInterfaceType);
        //            }
        //        }

        GraphQLObjectType graphQLObjectType = objectTypeBuilder.build();
        typeMap.put(type.getClassName(), graphQLObjectType);
    }

    private void addQueries(GraphQLSchema.Builder schemaBuilder) {

        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
                .name(QUERY)
                .description("Query root");

        if (schema.hasQueries()) {
            Set<Operation> queries = schema.getQueries();
            for (Operation queryOperation : queries) {
                GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinition(queryOperation);
                queryBuilder = queryBuilder.field(graphQLFieldDefinition);

                ReflectionDataFetcher datafetcher = new ReflectionDataFetcher(queryOperation);

                codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(QUERY,
                        graphQLFieldDefinition.getName()), datafetcher);
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
                GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinition(mutationOperation);
                mutationBuilder = mutationBuilder.field(graphQLFieldDefinition);

                ReflectionDataFetcher datafetcher = new ReflectionDataFetcher(mutationOperation);

                codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(MUTATION,
                        graphQLFieldDefinition.getName()), datafetcher);
            }

            GraphQLObjectType mutation = mutationBuilder.build();
            if (mutation.getFieldDefinitions() != null && !mutation.getFieldDefinitions().isEmpty()) {
                schemaBuilder.mutation(mutation);
            }
        }
    }

    private GraphQLFieldDefinition createGraphQLFieldDefinition(Operation operation) {
        // Fields
        GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name(operation.getName())
                .description(operation.getDescription());

        // Return field
        fieldBuilder = fieldBuilder.type(createGraphQLOutputType(operation));

        // Arguments
        if (operation.hasArguments()) {
            List<Argument> arguments = operation.getArguments();
            for (Field argument : arguments) {
                fieldBuilder = fieldBuilder.argument(createGraphQLArgument(argument));
            }
        }

        return fieldBuilder.build();
    }

    // TODO: Same as above ! ^^
    private GraphQLFieldDefinition createGraphQLFieldDefinition(Field field) {
        // Fields
        GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name(field.getName())
                .description(field.getDescription());

        // Return field
        fieldBuilder = fieldBuilder.type(createGraphQLOutputType(field));

        return fieldBuilder.build();
    }

    private GraphQLInputObjectField createGraphQLInputObjectField(Field field) {
        GraphQLInputObjectField.Builder inputFieldBuilder = GraphQLInputObjectField.newInputObjectField()
                .name(field.getName())
                .description(field.getDescription());

        // Type
        inputFieldBuilder = inputFieldBuilder.type(createGraphQLInputType(field));

        // Default value (on method)
        inputFieldBuilder = inputFieldBuilder.defaultValue(field.getDefaultValue());

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

    private GraphQLInputType createGraphQLInputType(Field field) {

        GraphQLInputType graphQLInputType = (GraphQLInputType) createGraphQLInputType(field.getReference());

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
        GraphQLOutputType graphQLOutputType = (GraphQLOutputType) createGraphQLOutputType(field.getReference());

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

    private GraphQLArgument createGraphQLArgument(Field parameter) {
        GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument()
                .name(parameter.getName())
                .description(parameter.getDescription())
                .defaultValue(parameter.getDefaultValue());

        GraphQLInputType createGraphQLType = createGraphQLInputType(parameter.getReference());

        argumentBuilder = argumentBuilder.type(createGraphQLType);

        return argumentBuilder.build();

    }

    private static final String QUERY = "Query";
    private static final String MUTATION = "Mutation";

}

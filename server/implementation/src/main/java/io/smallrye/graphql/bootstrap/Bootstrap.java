package io.smallrye.graphql.bootstrap;

import static graphql.schema.GraphQLList.list;
import static graphql.schema.visibility.DefaultGraphqlFieldVisibility.DEFAULT_FIELD_VISIBILITY;
import static graphql.schema.visibility.NoIntrospectionGraphqlFieldVisibility.NO_INTROSPECTION_FIELD_VISIBILITY;
import static io.smallrye.graphql.SmallRyeGraphQLServerLogging.log;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.bind.Jsonb;

import graphql.introspection.Introspection.DirectiveLocation;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.visibility.BlockedFields;
import graphql.schema.visibility.GraphqlFieldVisibility;
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.execution.datafetcher.BatchDataFetcher;
import io.smallrye.graphql.execution.datafetcher.CollectionCreator;
import io.smallrye.graphql.execution.datafetcher.FieldDataFetcher;
import io.smallrye.graphql.execution.error.ErrorInfoMap;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.execution.resolver.InterfaceOutputRegistry;
import io.smallrye.graphql.execution.resolver.InterfaceResolver;
import io.smallrye.graphql.json.JsonBCreator;
import io.smallrye.graphql.json.JsonInputRegistry;
import io.smallrye.graphql.scalar.GraphQLScalarTypes;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.DirectiveArgument;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.DirectiveType;
import io.smallrye.graphql.schema.model.EnumType;
import io.smallrye.graphql.schema.model.EnumValue;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Group;
import io.smallrye.graphql.schema.model.InputType;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.graphql.schema.model.Wrapper;
import io.smallrye.graphql.spi.ClassloadingService;
import io.smallrye.graphql.spi.LookupService;
import io.smallrye.graphql.spi.config.Config;

/**
 * Bootstrap MicroProfile GraphQL
 * This create a graphql-java model from the smallrye model
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Bootstrap {

    private final Schema schema;
    private final EventEmitter eventEmitter = EventEmitter.getInstance();
    private final DataFetcherFactory dataFetcherFactory = new DataFetcherFactory();
    private final Set<GraphQLDirective> directiveTypes = new LinkedHashSet<>();
    private final Map<String, GraphQLEnumType> enumMap = new HashMap<>();
    private final Map<String, GraphQLInterfaceType> interfaceMap = new HashMap<>();
    private final Map<String, GraphQLInputObjectType> inputMap = new HashMap<>();
    private final Map<String, GraphQLObjectType> typeMap = new HashMap<>();

    private GraphQLSchema graphQLSchema;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

    private final ClassloadingService classloadingService = ClassloadingService.get();

    public static GraphQLSchema bootstrap(Schema schema) {
        return bootstrap(schema, false);
    }

    public static GraphQLSchema bootstrap(Schema schema, boolean skipInjectionValidation) {
        if (schema != null && (schema.hasOperations())) {
            Bootstrap bootstrap = new Bootstrap(schema, skipInjectionValidation);
            bootstrap.generateGraphQLSchema();
            return bootstrap.graphQLSchema;
        } else {
            log.emptyOrNullSchema();
            return null;
        }
    }

    private Bootstrap(Schema schema, boolean skipInjectionValidation) {
        this.schema = schema;
        // setting `skipInjectionValidation` through a system property is not recommended,
        // but kept for backward compatibility for now
        if (!Boolean.getBoolean("test.skip.injection.validation") && !skipInjectionValidation) {
            verifyInjectionIsAvailable();
        }
    }

    /**
     * The owning class of each operation needs to be an injectable bean. If the LookupService can't
     * obtain an instance, probably it's either not a CDI bean, or beans.xml is missing.
     * This method verifies that all classes with operations can be injected through the LookupService.
     */
    private void verifyInjectionIsAvailable() {
        LookupService lookupService = LookupService.get();
        // This crazy stream operation basically collects all class names where we need to verify that
        // it belongs to an injectable bean
        Stream.of(
                schema.getQueries().stream().map(Operation::getClassName),
                schema.getMutations().stream().map(Operation::getClassName),
                schema.getGroupedQueries().values().stream().flatMap(Collection::stream).map(Operation::getClassName),
                schema.getGroupedMutations().values().stream().flatMap(Collection::stream).map(Operation::getClassName))
                .flatMap(stream -> stream)
                .distinct().forEach(beanClassName -> {
                    // verify that the bean is injectable
                    if (!lookupService.isResolvable(classloadingService.loadClass(beanClassName))) {
                        throw SmallRyeGraphQLServerMessages.msg.canNotInjectClass(beanClassName, null);
                    }
                });
    }

    private void generateGraphQLSchema() {
        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();

        createGraphQLDirectiveTypes();
        createGraphQLEnumTypes();
        createGraphQLInterfaceTypes();
        createGraphQLObjectTypes();
        createGraphQLInputObjectTypes();

        addQueries(schemaBuilder);
        addMutations(schemaBuilder);
        addSubscriptions(schemaBuilder);

        schemaBuilder.additionalDirectives(directiveTypes);
        schemaBuilder.additionalTypes(new HashSet<>(enumMap.values()));
        schemaBuilder.additionalTypes(new HashSet<>(interfaceMap.values()));
        schemaBuilder.additionalTypes(new HashSet<>(typeMap.values()));
        schemaBuilder.additionalTypes(new HashSet<>(inputMap.values()));

        this.codeRegistryBuilder.fieldVisibility(getGraphqlFieldVisibility());
        schemaBuilder = schemaBuilder.codeRegistry(codeRegistryBuilder.build());

        // register error info
        ErrorInfoMap.register(schema.getErrors());

        // Allow custom extension
        schemaBuilder = eventEmitter.fireBeforeSchemaBuild(schemaBuilder);

        Map<String, Jsonb> overrides = eventEmitter.fireOverrideJsonbConfig();
        JsonInputRegistry.override(overrides);

        this.graphQLSchema = schemaBuilder.build();
    }

    private void createGraphQLDirectiveTypes() {
        if (schema.hasDirectiveTypes()) {
            for (DirectiveType directiveType : schema.getDirectiveTypes()) {
                createGraphQLDirectiveType(directiveType);
            }
        }
    }

    private void createGraphQLDirectiveType(DirectiveType directiveType) {
        GraphQLDirective.Builder directiveBuilder = GraphQLDirective.newDirective()
                .name(directiveType.getName())
                .description(directiveType.getDescription());
        for (String location : directiveType.getLocations()) {
            directiveBuilder.validLocation(DirectiveLocation.valueOf(location));
        }
        for (String argumentName : directiveType.argumentNames()) {
            GraphQLInputType argumentType = argumentType(directiveType.argumentType(argumentName));
            directiveBuilder = directiveBuilder
                    .argument(GraphQLArgument.newArgument().type(argumentType).name(argumentName).build());
        }
        directiveBuilder.repeatable(directiveType.isRepeatable());
        directiveTypes.add(directiveBuilder.build());
    }

    private GraphQLInputType argumentType(DirectiveArgument argumentType) {
        GraphQLInputType inputType = getGraphQLInputType(argumentType.getReference());
        if (argumentType.hasWrapper() && argumentType.getWrapper().isCollectionOrArrayOrMap()) {
            inputType = list(inputType);
        }
        return inputType;
    }

    private void addQueries(GraphQLSchema.Builder schemaBuilder) {
        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
                .name(QUERY)
                .description(QUERY_DESCRIPTION);

        if (schema.hasQueries()) {
            addRootObject(queryBuilder, schema.getQueries(), QUERY);
        }
        if (schema.hasGroupedQueries()) {
            addGroupedRootObject(queryBuilder, schema.getGroupedQueries(), QUERY);
        }

        GraphQLObjectType query = queryBuilder.build();
        schemaBuilder.query(query);
    }

    private void addMutations(GraphQLSchema.Builder schemaBuilder) {
        GraphQLObjectType.Builder mutationBuilder = GraphQLObjectType.newObject()
                .name(MUTATION)
                .description(MUTATION_DESCRIPTION);

        if (schema.hasMutations()) {
            addRootObject(mutationBuilder, schema.getMutations(), MUTATION);
        }
        if (schema.hasGroupedMutations()) {
            addGroupedRootObject(mutationBuilder, schema.getGroupedMutations(), MUTATION);
        }

        GraphQLObjectType mutation = mutationBuilder.build();
        if (mutation.getFieldDefinitions() != null && !mutation.getFieldDefinitions().isEmpty()) {
            schemaBuilder.mutation(mutation);
        }
    }

    private void addSubscriptions(GraphQLSchema.Builder schemaBuilder) {
        GraphQLObjectType.Builder subscriptionBuilder = GraphQLObjectType.newObject()
                .name(SUBSCRIPTION)
                .description(SUBSCRIPTION_DESCRIPTION);

        if (schema.hasSubscriptions()) {
            addRootObject(subscriptionBuilder, schema.getSubscriptions(), SUBSCRIPTION);
        }
        if (schema.hasGroupedSubscriptions()) {
            addGroupedRootObject(subscriptionBuilder, schema.getGroupedSubscriptions(), SUBSCRIPTION);
        }

        GraphQLObjectType subscription = subscriptionBuilder.build();
        if (subscription.getFieldDefinitions() != null && !subscription.getFieldDefinitions().isEmpty()) {
            schemaBuilder.subscription(subscription);
        }
    }

    private void addRootObject(GraphQLObjectType.Builder rootBuilder, Set<Operation> operations,
            String rootName) {

        for (Operation operation : operations) {
            operation = eventEmitter.fireCreateOperation(operation);
            GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinitionFromOperation(rootName,
                    operation);
            rootBuilder.field(graphQLFieldDefinition);
        }
    }

    private void addGroupedRootObject(GraphQLObjectType.Builder rootBuilder,
            Map<Group, Set<Operation>> operationMap, String rootName) {
        Set<Map.Entry<Group, Set<Operation>>> operationsSet = operationMap.entrySet();

        for (Map.Entry<Group, Set<Operation>> operationsEntry : operationsSet) {
            Group group = operationsEntry.getKey();
            Set<Operation> operations = operationsEntry.getValue();

            GraphQLObjectType namedType = createNamedType(rootName, group, operations);

            GraphQLFieldDefinition.Builder graphQLFieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition()
                    .name(group.getName()).description(group.getDescription());

            graphQLFieldDefinitionBuilder.type(namedType);

            DataFetcher<?> dummyDataFetcher = dfe -> namedType.getName();

            GraphQLFieldDefinition namedField = graphQLFieldDefinitionBuilder.build();

            this.codeRegistryBuilder.dataFetcherIfAbsent(
                    FieldCoordinates.coordinates(rootName, namedField.getName()),
                    dummyDataFetcher);

            rootBuilder.field(namedField);
        }
    }

    private GraphQLObjectType createNamedType(String parent, Group group, Set<Operation> operations) {
        String namedTypeName = group.getName() + parent;
        GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject()
                .name(namedTypeName)
                .description(group.getDescription());

        // Operations
        for (Operation operation : operations) {
            operation = eventEmitter.fireCreateOperation(operation);

            GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinitionFromOperation(namedTypeName,
                    operation);
            objectTypeBuilder = objectTypeBuilder.field(graphQLFieldDefinition);
        }

        return objectTypeBuilder.build();
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
        for (EnumValue value : enumType.getValues()) {
            enumBuilder = enumBuilder.value(value.getValue(), value.getValue(), value.getDescription());
        }
        GraphQLEnumType graphQLEnumType = enumBuilder.build();
        enumMap.put(enumType.getClassName(), graphQLEnumType);
    }

    private void createGraphQLInterfaceTypes() {
        if (schema.hasInterfaces()) {
            for (Type interfaceType : schema.getInterfaces().values()) {
                createGraphQLInterfaceType(interfaceType);
            }
        }
    }

    private void createGraphQLInterfaceType(Type interfaceType) {
        GraphQLInterfaceType.Builder interfaceTypeBuilder = GraphQLInterfaceType.newInterface()
                .name(interfaceType.getName())
                .description(interfaceType.getDescription());

        // Fields
        if (interfaceType.hasFields()) {
            interfaceTypeBuilder = interfaceTypeBuilder
                    .fields(createGraphQLFieldDefinitionsFromFields(interfaceType,
                            interfaceType.getFields().values()));
        }

        // Interfaces
        if (interfaceType.hasInterfaces()) {
            Set<Reference> interfaces = interfaceType.getInterfaces();
            for (Reference i : interfaces) {
                interfaceTypeBuilder = interfaceTypeBuilder.withInterface(GraphQLTypeReference.typeRef(i.getName()));
            }
        }

        // Operations
        if (interfaceType.hasOperations()) {
            for (Operation operation : interfaceType.getOperations().values()) {
                String name = operation.getName();
                if (!interfaceType.hasBatchOperation(name)) {
                    operation = eventEmitter.fireCreateOperation(operation);

                    GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinitionFromOperation(
                            interfaceType.getName(),
                            operation);
                    interfaceTypeBuilder = interfaceTypeBuilder.field(graphQLFieldDefinition);
                } else {
                    log.duplicateOperation(operation.getName());
                }
            }
        }

        // Batch Operations
        if (interfaceType.hasBatchOperations()) {
            for (Operation operation : interfaceType.getBatchOperations().values()) {
                operation = eventEmitter.fireCreateOperation(operation);

                GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinitionFromBatchOperation(
                        interfaceType.getName(),
                        operation);
                interfaceTypeBuilder = interfaceTypeBuilder.field(graphQLFieldDefinition);
            }
        }

        GraphQLInterfaceType graphQLInterfaceType = interfaceTypeBuilder.build();
        // To resolve the concrete class
        this.codeRegistryBuilder.typeResolver(graphQLInterfaceType, new InterfaceResolver(interfaceType));
        this.interfaceMap.put(interfaceType.getName(), graphQLInterfaceType);
    }

    private void createGraphQLInputObjectTypes() {
        if (schema.hasInputs()) {
            for (InputType inputType : schema.getInputs().values()) {
                createGraphQLInputObjectType(inputType);
            }
        }
    }

    private GraphQLInputObjectType createGraphQLInputObjectType(InputType inputType) {
        GraphQLInputObjectType.Builder inputObjectTypeBuilder = GraphQLInputObjectType.newInputObject()
                .name(inputType.getName())
                .description(inputType.getDescription());

        // Fields
        if (inputType.hasFields()) {
            inputObjectTypeBuilder = inputObjectTypeBuilder
                    .fields(createGraphQLInputObjectFieldsFromFields(inputType.getFields().values()));
            // Register this input for posible JsonB usage
            JsonInputRegistry.register(inputType);
        }

        GraphQLInputObjectType graphQLInputObjectType = inputObjectTypeBuilder.build();
        if (!inputMap.containsKey(inputType.getName())) {
            inputMap.put(inputType.getName(), graphQLInputObjectType);
        }
        return graphQLInputObjectType;
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

        // Directives
        if (type.hasDirectiveInstances()) {
            for (DirectiveInstance directiveInstance : type.getDirectiveInstances()) {
                objectTypeBuilder.withDirective(createGraphQLDirectiveFrom(directiveInstance));
            }
        }

        // Fields
        if (type.hasFields()) {
            objectTypeBuilder = objectTypeBuilder
                    .fields(createGraphQLFieldDefinitionsFromFields(type, type.getFields().values()));
        }

        // Operations
        if (type.hasOperations()) {
            for (Operation operation : type.getOperations().values()) {
                String name = operation.getName();
                if (!type.hasBatchOperation(name)) {
                    operation = eventEmitter.fireCreateOperation(operation);

                    GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinitionFromOperation(type.getName(),
                            operation);
                    objectTypeBuilder = objectTypeBuilder.field(graphQLFieldDefinition);
                } else {
                    log.duplicateOperation(operation.getName());
                }
            }
        }

        // Batch Operations
        if (type.hasBatchOperations()) {
            for (Operation operation : type.getBatchOperations().values()) {
                operation = eventEmitter.fireCreateOperation(operation);

                GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinitionFromBatchOperation(type.getName(),
                        operation);
                objectTypeBuilder = objectTypeBuilder.field(graphQLFieldDefinition);
            }
        }

        // Interfaces
        if (type.hasInterfaces()) {
            Set<Reference> interfaces = type.getInterfaces();
            for (Reference i : interfaces) {
                if (interfaceMap.containsKey(i.getName())) {
                    GraphQLInterfaceType graphQLInterfaceType = interfaceMap.get(i.getName());
                    objectTypeBuilder = objectTypeBuilder.withInterface(graphQLInterfaceType);
                }
            }
        }

        GraphQLObjectType graphQLObjectType = objectTypeBuilder.build();
        typeMap.put(type.getName(), graphQLObjectType);

        // Register this output for interface type resolving
        InterfaceOutputRegistry.register(type, graphQLObjectType);
    }

    private GraphQLDirective createGraphQLDirectiveFrom(DirectiveInstance directiveInstance) {
        DirectiveType directiveType = directiveInstance.getType();
        GraphQLDirective.Builder directive = GraphQLDirective.newDirective();
        directive.name(directiveType.getName());
        directive.repeatable(directiveType.isRepeatable());
        for (Entry<String, Object> entry : directiveInstance.getValues().entrySet()) {
            String argumentName = entry.getKey();
            DirectiveArgument argumentType = directiveType.argumentType(argumentName);
            if (argumentType == null) {
                throw new IllegalArgumentException(
                        "Definition of directive type @" + directiveType.getName() + " does not contain" +
                                " an argument named " + argumentName + ", but directive instance " + directiveInstance
                                + " does contain a value for it");
            }
            directive.argument(GraphQLArgument.newArgument().type(argumentType(argumentType)).name(argumentName)
                    .value(entry.getValue()).build());
        }
        return directive.build();
    }

    private GraphQLFieldDefinition createGraphQLFieldDefinitionFromBatchOperation(String operationTypeName,
            Operation operation) {
        // Fields
        GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name(operation.getName())
                .description(operation.getDescription());

        // Return field
        fieldBuilder = fieldBuilder.type(createGraphQLOutputType(operation, true));

        // Arguments
        if (operation.hasArguments()) {
            fieldBuilder = fieldBuilder.arguments(createGraphQLArguments(operation.getArguments()));
        }

        DataFetcher<?> datafetcher = new BatchDataFetcher<>(operation, getTypeForField(operation));
        GraphQLFieldDefinition graphQLFieldDefinition = fieldBuilder.build();

        this.codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(operationTypeName, graphQLFieldDefinition.getName()),
                datafetcher);

        return graphQLFieldDefinition;
    }

    private GraphQLFieldDefinition createGraphQLFieldDefinitionFromOperation(String operationTypeName, Operation operation) {
        // Fields
        GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name(operation.getName())
                .description(operation.getDescription());

        // Return field
        fieldBuilder = fieldBuilder.type(createGraphQLOutputType(operation, false));

        // Arguments
        if (operation.hasArguments()) {
            fieldBuilder = fieldBuilder.arguments(createGraphQLArguments(operation.getArguments()));
        }
        // Auto Map argument
        Optional<GraphQLArgument> autoMapArgument = getAutoMapArgument(operation);
        if (autoMapArgument.isPresent()) {
            fieldBuilder.argument(autoMapArgument.get());
        }

        GraphQLFieldDefinition graphQLFieldDefinition = fieldBuilder.build();

        // DataFetcher
        DataFetcher<?> datafetcher = dataFetcherFactory.getDataFetcher(operation, getTypeForField(operation));

        this.codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(operationTypeName, graphQLFieldDefinition.getName()),
                datafetcher);

        return graphQLFieldDefinition;
    }

    private List<GraphQLFieldDefinition> createGraphQLFieldDefinitionsFromFields(Reference owner, Collection<Field> fields) {
        List<GraphQLFieldDefinition> graphQLFieldDefinitions = new ArrayList<>();
        for (Field field : fields) {
            graphQLFieldDefinitions.add(createGraphQLFieldDefinitionFromField(owner, field));
        }
        return graphQLFieldDefinitions;
    }

    private GraphQLFieldDefinition createGraphQLFieldDefinitionFromField(Reference owner, Field field) {
        GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name(field.getName())
                .description(field.getDescription());

        // Type
        fieldBuilder = fieldBuilder.type(createGraphQLOutputType(field, false));

        // Directives
        if (field.hasDirectiveInstances()) {
            for (DirectiveInstance directiveInstance : field.getDirectiveInstances()) {
                fieldBuilder.withDirective(createGraphQLDirectiveFrom(directiveInstance));
            }
        }

        // Auto Map argument
        Optional<GraphQLArgument> autoMapArgument = getAutoMapArgument(field);
        if (autoMapArgument.isPresent())
            fieldBuilder.argument(autoMapArgument.get());

        GraphQLFieldDefinition graphQLFieldDefinition = fieldBuilder.build();

        // DataFetcher
        FieldDataFetcher<?> datafetcher = new FieldDataFetcher<>(field, getTypeForField(field), owner);
        this.codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(owner.getName(), graphQLFieldDefinition.getName()),
                datafetcher);

        return graphQLFieldDefinition;
    }

    private Optional<GraphQLArgument> getAutoMapArgument(Field field) {
        // Auto Map argument
        if (field.hasWrapper() && field.getWrapper().isMap() && !field.isAdaptingWith()) { // TODO: Also pass this to the user adapter ?
            Map<String, Reference> parametrizedTypeArguments = field.getReference().getParametrizedTypeArguments();
            Reference keyReference = parametrizedTypeArguments.get(AUTOMAP_KEY_KEY);

            ReferenceType type = keyReference.getType();
            if (type.equals(ReferenceType.SCALAR) || type.equals(ReferenceType.ENUM)) {
                GraphQLInputType keyInput = getGraphQLInputType(keyReference);
                GraphQLArgument byKey = GraphQLArgument.newArgument()
                        .name(AUTOMAP_KEY_NAME)
                        .description(AUTOMAP_KEY_DESC)
                        .type(list(keyInput))
                        .build();

                return Optional.of(byKey);
            } else {
                String complexKeyName = keyReference.getName() + AUTOMAP_KEY_INPUT;
                if (schema.getInputs().containsKey(complexKeyName)) {
                    InputType complexKeyInputType = schema.getInputs().get(complexKeyName);
                    GraphQLInputObjectType keyInput;
                    if (inputMap.containsKey(complexKeyInputType.getName())) {
                        keyInput = inputMap.get(complexKeyInputType.getName());
                    } else {
                        keyInput = createGraphQLInputObjectType(complexKeyInputType);
                    }
                    GraphQLArgument byKey = GraphQLArgument.newArgument()
                            .name(AUTOMAP_KEY_NAME)
                            .description(AUTOMAP_KEY_DESC)
                            .type(list(keyInput))
                            .build();

                    return Optional.of(byKey);
                }
            }
        }
        return Optional.empty();
    }

    private List<GraphQLInputObjectField> createGraphQLInputObjectFieldsFromFields(Collection<Field> fields) {
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

        if (field.hasDirectiveInstances()) {
            for (DirectiveInstance directiveInstance : field.getDirectiveInstances()) {
                inputFieldBuilder.withDirective(createGraphQLDirectiveFrom(directiveInstance));
            }
        }

        // Default value (on method)
        if (field.hasDefaultValue()) {
            inputFieldBuilder = inputFieldBuilder.defaultValue(sanitizeDefaultValue(field));
        }

        return inputFieldBuilder.build();
    }

    private GraphQLInputType createGraphQLInputType(Field field) {

        GraphQLInputType graphQLInputType = referenceGraphQLInputType(field);

        Wrapper wrapper = dataFetcherFactory.unwrap(field, false);

        // Collection
        if (wrapper != null && wrapper.isCollectionOrArrayOrMap()) {
            // Mandatory in the collection
            if (wrapper.isNotEmpty()) {
                graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
            }
            // Collection depth
            do {
                if (wrapper.isCollectionOrArrayOrMap()) {
                    graphQLInputType = list(graphQLInputType);
                    wrapper = wrapper.getWrapper();
                } else {
                    wrapper = null;
                }
            } while (wrapper != null);
        }

        // Mandatory
        if (field.isNotNull()) {
            graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
        }

        return graphQLInputType;
    }

    private GraphQLOutputType createGraphQLOutputType(Field field, boolean isBatch) {
        GraphQLOutputType graphQLOutputType = referenceGraphQLOutputType(field);

        Wrapper wrapper = dataFetcherFactory.unwrap(field, isBatch);

        // Collection
        if (wrapper != null && wrapper.isCollectionOrArrayOrMap()) {
            // Mandatory in the collection
            if (wrapper.isNotEmpty()) {
                graphQLOutputType = GraphQLNonNull.nonNull(graphQLOutputType);
            }
            // Collection depth
            do {
                if (wrapper.isCollectionOrArrayOrMap()) {
                    graphQLOutputType = list(graphQLOutputType);
                    wrapper = wrapper.getWrapper();
                } else {
                    wrapper = null;
                }
            } while (wrapper != null);
        }

        // Mandatory
        if (field.isNotNull()) {
            graphQLOutputType = GraphQLNonNull.nonNull(graphQLOutputType);
        }

        return graphQLOutputType;
    }

    private GraphQLOutputType referenceGraphQLOutputType(Field field) {
        Reference reference = getCorrectFieldReference(field);
        ReferenceType type = reference.getType();
        String className = reference.getClassName();
        String name = reference.getName();
        switch (type) {
            case SCALAR:
                return getCorrectScalarType(reference);
            case ENUM:
                return enumMap.get(className);
            default:
                return GraphQLTypeReference.typeRef(name);
        }
    }

    private GraphQLInputType referenceGraphQLInputType(Field field) {
        Reference reference = getCorrectFieldReference(field);
        return getGraphQLInputType(reference);
    }

    private GraphQLInputType getGraphQLInputType(Reference reference) {
        ReferenceType type = reference.getType();
        String className = reference.getClassName();
        String name = reference.getName();
        switch (type) {
            case SCALAR:
                return getCorrectScalarType(reference);
            case ENUM:
                return enumMap.get(className);
            default:
                return GraphQLTypeReference.typeRef(name);
        }
    }

    private Reference getCorrectFieldReference(Field field) {
        if (field.getReference().isAdaptingWith()) {
            return field.getReference().getAdaptWith().getToReference();
        } else if (field.isAdaptingWith()) {
            return field.getAdaptWith().getToReference();
        } else if (field.getReference().isAdaptingTo()) {
            return field.getReference().getAdaptTo().getReference();
        } else if (field.isAdaptingTo()) {
            return field.getAdaptTo().getReference();
        } else {
            return field.getReference();
        }
    }

    private GraphQLScalarType getCorrectScalarType(Reference fieldReference) {
        return GraphQLScalarTypes.getScalarByName(fieldReference.getName());
    }

    private List<GraphQLArgument> createGraphQLArguments(List<Argument> arguments) {
        List<GraphQLArgument> graphQLArguments = new ArrayList<>();
        for (Argument argument : arguments) {
            if (!argument.isSourceArgument() && !IGNORABLE_ARGUMENTS.contains(argument.getReference().getClassName())) {
                graphQLArguments.add(createGraphQLArgument(argument));
            }
        }
        return graphQLArguments;
    }

    private GraphQLArgument createGraphQLArgument(Argument argument) {
        GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument()
                .name(argument.getName())
                .description(argument.getDescription());

        if (argument.hasDefaultValue()) {
            argumentBuilder = argumentBuilder.defaultValue(sanitizeDefaultValue(argument));
        }

        GraphQLInputType graphQLInputType = referenceGraphQLInputType(argument);

        Wrapper wrapper = dataFetcherFactory.unwrap(argument, false);

        // Collection
        if (wrapper != null && wrapper.isCollectionOrArrayOrMap()) {
            // Mandatory in the collection
            if (wrapper.isNotEmpty()) {
                graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
            }
            // Collection depth
            do {
                if (wrapper.isCollectionOrArrayOrMap()) {
                    graphQLInputType = list(graphQLInputType);
                    wrapper = wrapper.getWrapper();
                } else {
                    wrapper = null;
                }
            } while (wrapper != null);
        }

        // Mandatory
        if (argument.isNotNull()) {
            graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
        }

        argumentBuilder = argumentBuilder.type(graphQLInputType);

        if (argument.hasDirectiveInstances()) {
            for (DirectiveInstance directiveInstance : argument.getDirectiveInstances()) {
                argumentBuilder.withDirective(createGraphQLDirectiveFrom(directiveInstance));
            }
        }

        return argumentBuilder.build();

    }

    private Object sanitizeDefaultValue(Field field) {
        String jsonString = field.getDefaultValue();

        if (jsonString == null) {
            return null;
        }

        if (isJsonString(jsonString)) {
            Class<?> deserType;
            Wrapper wrapper = dataFetcherFactory.unwrap(field, false);

            if (wrapper != null && wrapper.isCollectionOrArrayOrMap()) {
                deserType = classloadingService.loadClass(field.getWrapper().getWrapperClassName());
                if (Collection.class.isAssignableFrom(deserType)) {
                    deserType = CollectionCreator.newCollection(field.getWrapper().getWrapperClassName(), 0).getClass();
                }
            } else {
                Reference reference = getCorrectFieldReference(field);
                ReferenceType referenceType = reference.getType();

                if (referenceType.equals(ReferenceType.INPUT) || referenceType.equals(ReferenceType.TYPE)) {
                    deserType = Map.class;
                } else {
                    deserType = classloadingService.loadClass(field.getReference().getClassName());
                }
            }

            Jsonb jsonB = JsonBCreator.getJsonB(deserType.getName());
            return jsonB.fromJson(jsonString, deserType);
        }

        if (Classes.isNumberLikeType(field.getReference().getGraphQlClassName())) {
            return new BigDecimal(jsonString);
        }

        if (Classes.isBoolean(field.getReference().getGraphQlClassName())) {
            return Boolean.parseBoolean(jsonString);
        }

        return jsonString;
    }

    private boolean isJsonString(String string) {
        if (string != null && !string.isEmpty() && (string.contains("{") || string.contains("["))) {
            try (StringReader stringReader = new StringReader(string);
                    JsonReader jsonReader = jsonReaderFactory.createReader(stringReader)) {

                jsonReader.readValue();
                return true;
            } catch (Exception ex) {
                // Not a valid json
            }
        }
        return false;
    }

    /**
     * This can hide certain fields in the schema (for security purposes)
     *
     * @return The visibility
     * @see <a href="www.graphql-java.com/documentation/v15/fieldvisibility/">GraphQL Java Field Visibility</a>
     */
    private GraphqlFieldVisibility getGraphqlFieldVisibility() {
        Config config = Config.get();
        String fieldVisibility = config.getFieldVisibility();
        if (fieldVisibility != null && !fieldVisibility.isEmpty()) {

            if (fieldVisibility.equals(Config.FIELD_VISIBILITY_NO_INTROSPECTION)) {
                return NO_INTROSPECTION_FIELD_VISIBILITY;
            } else {
                String[] patterns = fieldVisibility.split(COMMA);
                BlockedFields.Builder blockedFields = BlockedFields.newBlock();
                for (String pattern : patterns) {
                    blockedFields = blockedFields.addPattern(pattern);
                }
                return blockedFields.build();
            }
        }
        return DEFAULT_FIELD_VISIBILITY;
    }

    private Type getTypeForField(Field field) {
        Map<String, Type> types = schema.getTypes();
        return types.get(field.getReference().getName());
    }

    private static final String QUERY = "Query";
    private static final String QUERY_DESCRIPTION = "Query root";

    private static final String MUTATION = "Mutation";
    private static final String MUTATION_DESCRIPTION = "Mutation root";

    private static final String SUBSCRIPTION = "Subscription";
    private static final String SUBSCRIPTION_DESCRIPTION = "Subscription root";

    private static final String COMMA = ",";

    private static final JsonReaderFactory jsonReaderFactory = Json.createReaderFactory(null);

    private static final String CONTEXT = "io.smallrye.graphql.api.Context";
    private static final String OBSERVES = "javax.enterprise.event.Observes";

    private static final List<String> IGNORABLE_ARGUMENTS = Arrays.asList(CONTEXT, OBSERVES);

    private static final String AUTOMAP_KEY_NAME = "key";
    private static final String AUTOMAP_KEY_DESC = "Get entry/entries for a certain key/s";
    private static final String AUTOMAP_KEY_KEY = "K";
    private static final String AUTOMAP_KEY_INPUT = "Input";

}

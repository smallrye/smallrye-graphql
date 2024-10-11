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
import java.util.Stack;
import java.util.stream.Collectors;

import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.bind.Jsonb;

import org.eclipse.microprofile.graphql.Name;

import com.apollographql.federation.graphqljava.Federation;

import graphql.Scalars;
import graphql.introspection.Introspection.DirectiveLocation;
import graphql.schema.Coercing;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLEnumValueDefinition;
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
import graphql.schema.GraphQLUnionType;
import graphql.schema.TypeResolver;
import graphql.schema.visibility.BlockedFields;
import graphql.schema.visibility.GraphqlFieldVisibility;
import io.smallrye.graphql.JsonProviderHolder;
import io.smallrye.graphql.SmallRyeGraphQLServerMessages;
import io.smallrye.graphql.execution.Classes;
import io.smallrye.graphql.execution.datafetcher.BatchDataFetcher;
import io.smallrye.graphql.execution.datafetcher.CollectionCreator;
import io.smallrye.graphql.execution.datafetcher.PlugableDataFetcher;
import io.smallrye.graphql.execution.error.ErrorInfoMap;
import io.smallrye.graphql.execution.event.EventEmitter;
import io.smallrye.graphql.execution.resolver.InterfaceOutputRegistry;
import io.smallrye.graphql.execution.resolver.InterfaceResolver;
import io.smallrye.graphql.execution.resolver.UnionOutputRegistry;
import io.smallrye.graphql.execution.resolver.UnionResolver;
import io.smallrye.graphql.json.JsonBCreator;
import io.smallrye.graphql.json.JsonInputRegistry;
import io.smallrye.graphql.scalar.GraphQLScalarTypes;
import io.smallrye.graphql.scalar.custom.FloatCoercing;
import io.smallrye.graphql.scalar.custom.IntCoercing;
import io.smallrye.graphql.scalar.custom.StringCoercing;
import io.smallrye.graphql.schema.model.Argument;
import io.smallrye.graphql.schema.model.CustomScalarType;
import io.smallrye.graphql.schema.model.CustomScalarType.CustomScalarPrimitiveType;
import io.smallrye.graphql.schema.model.DirectiveArgument;
import io.smallrye.graphql.schema.model.DirectiveInstance;
import io.smallrye.graphql.schema.model.DirectiveType;
import io.smallrye.graphql.schema.model.EnumType;
import io.smallrye.graphql.schema.model.EnumValue;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.InputType;
import io.smallrye.graphql.schema.model.NamespaceContainer;
import io.smallrye.graphql.schema.model.Operation;
import io.smallrye.graphql.schema.model.Reference;
import io.smallrye.graphql.schema.model.ReferenceType;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.schema.model.Type;
import io.smallrye.graphql.schema.model.UnionType;
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
    private final Map<String, GraphQLUnionType> unionMap = new HashMap<>();
    private final Map<String, GraphQLInputObjectType> inputMap = new HashMap<>();
    private final Map<String, GraphQLObjectType> typeMap = new HashMap<>();

    private GraphQLSchema graphQLSchema;
    private final GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

    private final ClassloadingService classloadingService = ClassloadingService.get();

    private LinkProcessor linkProcessor = null;

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
        if (Config.get().isFederationEnabled()) {
            this.linkProcessor = new LinkProcessor(schema);
        }
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
        schema.getAllOperations().stream()
                .map(Operation::getClassName)
                .distinct().forEach(beanClassName -> {
                    // verify that the bean is injectable
                    if (!lookupService.isResolvable(classloadingService.loadClass(beanClassName))) {
                        throw SmallRyeGraphQLServerMessages.msg.canNotInjectClass(beanClassName, null);
                    }
                });
    }

    private void generateGraphQLSchema() {
        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();

        if (Config.get().isFederationEnabled()) {
            linkProcessor.createLinkImports();
        }

        createGraphQLCustomScalarTypes();
        createGraphQLEnumTypes();
        createGraphQLDirectiveTypes();

        createGraphQLInterfaceTypes();
        createGraphQLUnionTypes();
        createGraphQLObjectTypes();
        createGraphQLInputObjectTypes();

        GraphQLObjectType queryRootType = addQueries(schemaBuilder);
        addMutations(schemaBuilder);
        addSubscriptions(schemaBuilder);
        schemaBuilder.withSchemaAppliedDirectives(Arrays.stream(
                createGraphQLDirectives(schema.getDirectiveInstances()))
                .map(GraphQLDirective::toAppliedDirective)
                .collect(Collectors.toList()));
        schemaBuilder.description(schema.getDescription());
        schemaBuilder.additionalDirectives(directiveTypes);
        schemaBuilder.additionalTypes(new HashSet<>(enumMap.values()));
        schemaBuilder.additionalTypes(new HashSet<>(interfaceMap.values()));
        schemaBuilder.additionalTypes(new HashSet<>(unionMap.values()));
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

        if (Config.get().isFederationEnabled()) {
            log.enableFederation();

            // Hack: Prevents schema build errors when queries are empty.
            // Will be overridden during the Federation transformation.
            addDummySdlQuery(schemaBuilder, queryRootType);

            // Build reference resolvers type, without adding to schema (just for federation)
            GraphQLObjectType resolversType = buildResolvers();

            GraphQLSchema rawSchema = schemaBuilder.build();
            this.graphQLSchema = Federation.transform(rawSchema)
                    .fetchEntities(
                            new FederationDataFetcher(resolversType, rawSchema.getQueryType(), rawSchema.getCodeRegistry()))
                    .resolveEntityType(fetchEntityType())
                    .setFederation2(true)
                    .build();
        } else {
            this.graphQLSchema = schemaBuilder.build();
        }
    }

    private void addDummySdlQuery(GraphQLSchema.Builder schemaBuilder, GraphQLObjectType queryRootType) {
        GraphQLObjectType type = GraphQLObjectType.newObject()
                .name("_Service")
                .field(GraphQLFieldDefinition
                        .newFieldDefinition().name("sdl")
                        .type(new GraphQLNonNull(Scalars.GraphQLString))
                        .build())
                .build();

        GraphQLFieldDefinition field = GraphQLFieldDefinition.newFieldDefinition()
                .name("_service")
                .type(GraphQLNonNull.nonNull(type))
                .build();

        GraphQLObjectType.Builder newQueryType = GraphQLObjectType.newObject(queryRootType);

        newQueryType.field(field);
        schemaBuilder.query(newQueryType.build());
    }

    private GraphQLObjectType buildResolvers() {
        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
                .name("Resolver");
        if (schema.hasResolvers()) {
            addRootObject(queryBuilder, schema.getResolvers(), "Resolver");
        }
        return queryBuilder.build();
    }

    private TypeResolver fetchEntityType() {
        return env -> {
            Object src = env.getObject();
            if (src == null) {
                return null;
            }
            Name annotation = src.getClass().getAnnotation(Name.class);
            String typeName = (annotation == null) ? src.getClass().getSimpleName() : annotation.value();
            GraphQLObjectType result = env.getSchema().getObjectType(typeName);
            if (result == null) {
                throw new RuntimeException("can't resolve federated entity type " + src.getClass().getName());
            }
            return result;
        };
    }

    private void createGraphQLCustomScalarTypes() {
        if (schema.hasCustomScalarTypes()) {
            for (CustomScalarType customScalarType : schema.getCustomScalarTypes()) {
                createGraphQLCustomScalarType(customScalarType);
            }
        }
    }

    private void createGraphQLCustomScalarType(CustomScalarType customScalarType) {
        String scalarName = customScalarType.getName();

        String description = getDescription(customScalarType);

        Coercing<?, ?> coercing = getCoercing(customScalarType);

        GraphQLScalarType graphQLScalarType = GraphQLScalarType.newScalar()
                .name(scalarName)
                .description(description)
                .coercing(coercing)
                .build();

        GraphQLScalarTypes.registerCustomScalar(
                scalarName,
                customScalarType.getClassName(),
                graphQLScalarType);
    }

    private static String getDescription(CustomScalarType customScalarType) {
        return Optional.ofNullable(customScalarType.getDescription())
                .orElse("Scalar for " + customScalarType.getName());
    }

    private static Coercing<?, ?> getCoercing(CustomScalarType customScalarType) {
        CustomScalarPrimitiveType primitiveType = customScalarType.getCustomScalarPrimitiveType();

        Coercing<?, ?> coercing = null;
        switch (primitiveType) {
            case STRING_TYPE:
                coercing = new StringCoercing(customScalarType.getClassName());
                break;
            case INT_TYPE:
                coercing = new IntCoercing(customScalarType.getClassName());
                break;
            case FLOAT_TYPE:
                coercing = new FloatCoercing(customScalarType.getClassName());
                break;
        }
        return coercing;
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
                .name(Config.get().isFederationEnabled() ? linkProcessor.newNameDirective(directiveType.getName())
                        : directiveType.getName())
                .description(directiveType.getDescription());
        for (String location : directiveType.getLocations()) {
            directiveBuilder.validLocation(DirectiveLocation.valueOf(location));
        }
        for (String argumentName : directiveType.argumentNames()) {
            DirectiveArgument argumentType = directiveType.argumentType(argumentName);
            GraphQLInputType inputType = createGraphQLInputType(argumentType);

            GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument()
                    .name(argumentName)
                    .type(inputType);

            if (argumentType.hasDefaultValue()) {
                argumentBuilder = argumentBuilder.defaultValueProgrammatic(
                        sanitizeDefaultValue(argumentType));
            }

            directiveBuilder = directiveBuilder.argument(argumentBuilder.build());
        }
        directiveBuilder.repeatable(directiveType.isRepeatable());
        directiveTypes.add(directiveBuilder.build());
    }

    private GraphQLObjectType addQueries(GraphQLSchema.Builder schemaBuilder) {
        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
                .name(QUERY)
                .description(QUERY_DESCRIPTION);

        if (schema.hasQueries()) {
            addRootObject(queryBuilder, schema.getQueries(), QUERY);
        }
        if (schema.hasNamespaceQueries()) {
            addNamespacedRootObject(queryBuilder, schema.getNamespacedQueries(), QUERY);
        }

        GraphQLObjectType query = queryBuilder.build();
        schemaBuilder.query(query);
        return query;
    }

    private void addMutations(GraphQLSchema.Builder schemaBuilder) {
        GraphQLObjectType.Builder mutationBuilder = GraphQLObjectType.newObject()
                .name(MUTATION)
                .description(MUTATION_DESCRIPTION);

        if (schema.hasMutations()) {
            addRootObject(mutationBuilder, schema.getMutations(), MUTATION);
        }
        if (schema.hasNamespaceMutations()) {
            addNamespacedRootObject(mutationBuilder, schema.getNamespacedMutations(), MUTATION);
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

    private String makeFirstLetterUppercase(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    private void addNamespacedRootObject(GraphQLObjectType.Builder rootBuilder,
            Map<String, NamespaceContainer> namespaceMutations, String mutation) {
        namespaceMutations.values()
                .forEach(groupContainer -> addNamespacedRootObject(rootBuilder, groupContainer, "", mutation));
    }

    private List<GraphQLFieldDefinition> addNamespacedRootObject(GraphQLObjectType.Builder rootBuilder,
            NamespaceContainer groupContainer, String rootName, String suffix) {
        List<GraphQLFieldDefinition> graphQLFieldDefinitions = groupContainer.getContainer().isEmpty()
                ? List.of()
                : getGraphQLFieldDefinition(groupContainer, rootName, suffix);

        if (groupContainer.getOperations().isEmpty() && graphQLFieldDefinitions.isEmpty()) {
            return List.of();
        }

        GraphQLObjectType namedType = createNamespaceType(rootName, suffix, groupContainer,
                groupContainer.getOperations(), graphQLFieldDefinitions);

        GraphQLFieldDefinition.Builder graphQLFieldDefinitionBuilder = GraphQLFieldDefinition
                .newFieldDefinition()
                .name(groupContainer.getName())
                .description(groupContainer.getDescription());

        graphQLFieldDefinitionBuilder.type(namedType);

        DataFetcher<?> dummyDataFetcher = dfe -> namedType.getName();

        GraphQLFieldDefinition namedField = graphQLFieldDefinitionBuilder.build();

        this.codeRegistryBuilder.dataFetcherIfAbsent(
                FieldCoordinates.coordinates(rootName + suffix, namedField.getName()),
                dummyDataFetcher);
        rootBuilder.field(namedField);

        return List.of(namedField);
    }

    private List<GraphQLFieldDefinition> getGraphQLFieldDefinition(NamespaceContainer groupContainer, String rootName,
            String suffix) {
        String name = makeFirstLetterUppercase(groupContainer.getName());
        String namedTypeName = rootName + name + suffix;

        GraphQLObjectType.Builder wrapperBuilder = GraphQLObjectType.newObject()
                .name(namedTypeName)
                .description(groupContainer.getDescription());

        return groupContainer
                .getContainer()
                .values()
                .stream()
                .map(namespace -> addNamespacedRootObject(
                        wrapperBuilder, namespace, rootName + makeFirstLetterUppercase(groupContainer.getName()), suffix))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private GraphQLObjectType createNamespaceType(String root, String suffix, NamespaceContainer namespace,
            Set<Operation> operations, List<GraphQLFieldDefinition> graphQLFieldDefinitions) {
        String name = makeFirstLetterUppercase(namespace.getName());

        String namedTypeName = root + name + suffix;
        GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject()
                .name(namedTypeName)
                .description(namespace.getDescription());

        // Operations
        for (Operation operation : operations) {
            operation = eventEmitter.fireCreateOperation(operation);

            GraphQLFieldDefinition graphQLFieldDefinition = createGraphQLFieldDefinitionFromOperation(
                    namedTypeName, operation);
            objectTypeBuilder = objectTypeBuilder.field(graphQLFieldDefinition);
        }

        objectTypeBuilder.fields(graphQLFieldDefinitions);
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
                .name(Config.get().isFederationEnabled() ? linkProcessor.newName(enumType.getName())
                        : enumType.getName())
                .description(enumType.getDescription());
        // Directives
        if (enumType.hasDirectiveInstances()) {
            enumBuilder = enumBuilder.withDirectives(createGraphQLDirectives(enumType.getDirectiveInstances()));
        }
        // Values
        for (EnumValue value : enumType.getValues()) {
            GraphQLEnumValueDefinition.Builder definitionBuilder = GraphQLEnumValueDefinition.newEnumValueDefinition()
                    .name(value.getValue())
                    .value(value.getValue())
                    .description(value.getDescription());
            if (value.hasDirectiveInstances()) {
                definitionBuilder = definitionBuilder.withDirectives(createGraphQLDirectives(value.getDirectiveInstances()));
            }
            enumBuilder = enumBuilder.value(definitionBuilder.build());
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

        // Directives
        if (interfaceType.hasDirectiveInstances()) {
            for (DirectiveInstance directiveInstance : interfaceType.getDirectiveInstances()) {
                interfaceTypeBuilder.withDirective(createGraphQLDirectiveFrom(directiveInstance));
            }
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

    private void createGraphQLUnionTypes() {
        // We can't create unions if there are no types to be a member of them
        if (schema.hasUnions() && schema.hasTypes()) {
            for (UnionType unionType : schema.getUnions().values()) {
                createGraphQLUnionType(unionType);
            }
        }
    }

    private void createGraphQLUnionType(UnionType unionType) {
        GraphQLUnionType.Builder unionTypeBuilder = GraphQLUnionType.newUnionType()
                .name(unionType.getName())
                .description(unionType.getDescription());

        // Members
        for (Type type : schema.getTypes().values()) {
            if (type.isMemberOfUnion(unionType)) {
                unionTypeBuilder.possibleType(GraphQLTypeReference.typeRef(type.getName()));
            }
        }

        // Directives
        if (unionType.hasDirectiveInstances()) {
            for (DirectiveInstance directiveInstance : unionType.getDirectiveInstances()) {
                unionTypeBuilder.withDirective(createGraphQLDirectiveFrom(directiveInstance));
            }
        }

        GraphQLUnionType graphQLUnionType = unionTypeBuilder.build();
        // To resolve the concrete class
        this.codeRegistryBuilder.typeResolver(graphQLUnionType, new UnionResolver(unionType));
        this.unionMap.put(unionType.getName(), graphQLUnionType);

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

        // Directives
        if (inputType.hasDirectiveInstances()) {
            inputObjectTypeBuilder = inputObjectTypeBuilder
                    .withDirectives(createGraphQLDirectives(inputType.getDirectiveInstances()));
        }

        // Fields
        if (inputType.hasFields()) {
            inputObjectTypeBuilder = inputObjectTypeBuilder
                    .fields(createGraphQLInputObjectFieldsFromFields(inputType.getFields().values()));
            // Register this input for possible JsonB usage
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

        // Register this output for interface/union type resolving
        InterfaceOutputRegistry.register(type, graphQLObjectType);
        UnionOutputRegistry.register(type, graphQLObjectType);
    }

    private GraphQLDirective createGraphQLDirectiveFrom(DirectiveInstance directiveInstance) {
        DirectiveType directiveType = directiveInstance.getType();
        GraphQLDirective.Builder directiveBuilder = GraphQLDirective.newDirective()
                .name(Config.get().isFederationEnabled() ? linkProcessor.newNameDirective(directiveType.getName())
                        : directiveType.getName())
                .repeatable(directiveType.isRepeatable());
        for (Entry<String, Object> entry : directiveInstance.getValues().entrySet()) {
            String argumentName = entry.getKey();
            DirectiveArgument argumentType = directiveType.argumentType(argumentName);
            if (argumentType == null) {
                throw new IllegalArgumentException(
                        "Definition of directive type @" + directiveType.getName() + " does not contain" +
                                " an argument named " + argumentName + ", but directive instance " + directiveInstance
                                + " does contain a value for it");
            }
            GraphQLInputType inputType = createGraphQLInputType(argumentType);

            // We have to call .valueProgrammatic(...) in order for graphql.schema.InputValueWithState to be set to
            // EXTERNAL_VALUE, meaning that .valueToLiteral(...) from our own Coercing implementation will be called
            // when converting. If we use .value(...), INTERNAL_VALUE (which is deprecated) will be used, along with
            // ValuesResolverLegacy.valueToLiteralLegacy
            GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument()
                    .name(argumentName)
                    .type(inputType)
                    .valueProgrammatic(entry.getValue());

            if (argumentType.hasDefaultValue()) {
                argumentBuilder = argumentBuilder.defaultValueProgrammatic(sanitizeDefaultValue(argumentType));
            }

            directiveBuilder.argument(argumentBuilder.build());
        }

        for (String argumentName : directiveType.argumentNames()) {
            // Check if this argument is not already defined in directiveInstance
            if (!directiveInstance.getValues().containsKey(argumentName)) {
                DirectiveArgument argumentType = directiveType.argumentType(argumentName);
                // If the argument is required and has no default value, throw an exception
                if (argumentType.isNotNull() && !argumentType.hasDefaultValue()) {
                    throw new IllegalArgumentException(
                            "Definition of type @" + directiveType.getName() + " contains a required argument named " +
                                    argumentName + ", but directive instance " + directiveInstance +
                                    " does not contain a value for it");
                }
                GraphQLInputType inputType = createGraphQLInputType(argumentType);

                GraphQLArgument.Builder argumentBuilder = GraphQLArgument.newArgument()
                        .name(argumentName)
                        .type(inputType);

                if (argumentType.hasDefaultValue()) {
                    argumentBuilder = argumentBuilder.defaultValueProgrammatic(sanitizeDefaultValue(argumentType));
                }

                directiveBuilder.argument(argumentBuilder.build());
            }
        }
        directiveBuilder.validLocations(directiveInstance
                .getType()
                .getLocations()
                .stream()
                .map(location -> DirectiveLocation.valueOf(DirectiveLocation.class, location))
                .toArray(DirectiveLocation[]::new));

        return directiveBuilder.build();
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

        // Directives
        if (operation.hasDirectiveInstances()) {
            fieldBuilder = fieldBuilder.withDirectives(createGraphQLDirectives(operation.getDirectiveInstances()));
        }

        GraphQLFieldDefinition graphQLFieldDefinition = fieldBuilder.build();

        // DataFetcher
        DataFetcher<?> datafetcher = dataFetcherFactory.getDataFetcher(operation, getTypeForField(operation));

        this.codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(operationTypeName, graphQLFieldDefinition.getName()),
                datafetcher);

        return graphQLFieldDefinition;
    }

    private GraphQLDirective[] createGraphQLDirectives(Collection<DirectiveInstance> directiveInstances) {
        return directiveInstances.stream()
                .map(this::createGraphQLDirectiveFrom)
                .toArray(GraphQLDirective[]::new);
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
        PlugableDataFetcher<?> datafetcher = dataFetcherFactory.getFieldDataFetcher(field, getTypeForField(field), owner);
        this.codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(owner.getName(), graphQLFieldDefinition.getName()),
                datafetcher);

        return graphQLFieldDefinition;
    }

    private Optional<GraphQLArgument> getAutoMapArgument(Field field) {
        // Auto Map argument
        if (field.hasWrapper() && field.getWrapper().isMap() && !field.isAdaptingWith()) { // TODO: Also pass this to the user adapter ?
            Map<String, Reference> parametrizedTypeArguments = field.getReference().getAllParametrizedTypes();
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
            inputFieldBuilder = inputFieldBuilder.defaultValueProgrammatic(sanitizeDefaultValue(field));
        }

        return inputFieldBuilder.build();
    }

    private GraphQLInputType createGraphQLInputType(Field field) {

        GraphQLInputType graphQLInputType = referenceGraphQLInputType(field);
        Wrapper wrapper = dataFetcherFactory.unwrap(field, false);
        // Field can have a wrapper, like List<String>
        if (wrapper != null && wrapper.isCollectionOrArrayOrMap()) {
            Stack<Wrapper> stackOfWrappers = new Stack<>();
            for (Wrapper currentWrapper = wrapper; currentWrapper != null; currentWrapper = currentWrapper.getWrapper()) {
                stackOfWrappers.add(currentWrapper);
            }
            // Loop as long as there is a wrapper
            do {
                wrapper = stackOfWrappers.pop();
                if (wrapper.isCollectionOrArrayOrMap()) {
                    if (wrapper.isWrappedTypeNotNull()) {
                        graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
                    }
                    graphQLInputType = list(graphQLInputType);
                }
            } while (!stackOfWrappers.empty());
        }

        // Check if field is mandatory
        if (field.isNotNull()) {
            graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
        }

        return graphQLInputType;
    }

    private GraphQLOutputType createGraphQLOutputType(Field field, boolean isBatch) {
        GraphQLOutputType graphQLOutputType = referenceGraphQLOutputType(field);

        Wrapper wrapper = dataFetcherFactory.unwrap(field, isBatch);
        // Field can have a wrapper, like List<String>
        if (wrapper != null && wrapper.isCollectionOrArrayOrMap()) {
            Stack<Wrapper> stackOfWrappers = new Stack<>();
            for (Wrapper currentWrapper = wrapper; currentWrapper != null; currentWrapper = currentWrapper.getWrapper()) {
                stackOfWrappers.add(currentWrapper);
            }
            // Loop as long as there is a wrapper
            do {
                wrapper = stackOfWrappers.pop();
                if (wrapper.isCollectionOrArrayOrMap()) {
                    if (wrapper.isWrappedTypeNotNull()) {
                        graphQLOutputType = GraphQLNonNull.nonNull(graphQLOutputType);
                    }
                    graphQLOutputType = list(graphQLOutputType);
                }
            } while (!stackOfWrappers.empty());
        }

        // Check if field is mandatory
        if (field.isNotNull()) {
            graphQLOutputType = GraphQLNonNull.nonNull(graphQLOutputType);
        }

        return graphQLOutputType;
    }

    private GraphQLInputType referenceGraphQLInputType(Field field) {
        Reference reference = getCorrectFieldReference(field);
        return getGraphQLInputType(reference);
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
        // Since we can rename scalar types using the link directive, but GraphQLScalarTypes has a static definition
        // of scalar types, we need to check if the scalar type has been renamed and if so, create a new one
        GraphQLScalarType graphQLScalarType = GraphQLScalarTypes.getScalarByName(fieldReference.getName());
        String newName = Config.get().isFederationEnabled() ? linkProcessor.newName(fieldReference.getName())
                : fieldReference.getName();
        if (fieldReference.getName().equals(newName)) {
            // If the field name equals the processed name, simply return the static scalar type
            return graphQLScalarType;
        }
        GraphQLScalarType newGraphQLScalarType = GraphQLScalarTypes.getScalarByName(newName);
        if (newGraphQLScalarType == null) {
            // If the processed name does not yet exist, create a new one and register it
            newGraphQLScalarType = GraphQLScalarType.newScalar(graphQLScalarType)
                    .name(newName)
                    .build();
            GraphQLScalarTypes.registerCustomScalar(newName, fieldReference.getClassName(), newGraphQLScalarType);
        }
        return newGraphQLScalarType;
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
            argumentBuilder = argumentBuilder.defaultValueProgrammatic(sanitizeDefaultValue(argument));
        }

        GraphQLInputType graphQLInputType = createGraphQLInputType(argument);
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

        if (Classes.isNumberLikeType(field.getReference().getGraphQLClassName())) {
            return new BigDecimal(jsonString);
        }

        if (Classes.isBoolean(field.getReference().getGraphQLClassName())) {
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

    private static final JsonReaderFactory jsonReaderFactory = JsonProviderHolder.JSON_PROVIDER.createReaderFactory(null);

    private static final String CONTEXT = "io.smallrye.graphql.api.Context";
    private static final String OBSERVES = "javax.enterprise.event.Observes";

    private static final List<String> IGNORABLE_ARGUMENTS = Arrays.asList(CONTEXT, OBSERVES);

    private static final String AUTOMAP_KEY_NAME = "key";
    private static final String AUTOMAP_KEY_DESC = "Get entry/entries for a certain key/s";
    private static final String AUTOMAP_KEY_KEY = "K";
    private static final String AUTOMAP_KEY_INPUT = "Input";

}

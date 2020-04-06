package io.smallrye.graphql.bootstrap;

import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;

import graphql.Scalars;
import graphql.schema.FieldCoordinates;
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
import graphql.schema.GraphQLType;
import io.smallrye.graphql.bootstrap.datafetcher.CdiReflectionDataFetcher;
import io.smallrye.graphql.bootstrap.typeresolver.InterfaceResolver;
import io.smallrye.graphql.schema.model.Complex;
import io.smallrye.graphql.schema.model.Enum;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Method;
import io.smallrye.graphql.schema.model.ReferenceType;
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

    public static GraphQLSchema bootstrap(Schema schema) {
        Bootstrap graphQLBootstrap = new Bootstrap(schema);
        return graphQLBootstrap.generateGraphQLSchema();
    }

    private Bootstrap(Schema schema) {
        this.schema = schema;
    }

    private GraphQLSchema generateGraphQLSchema() {
        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();

        addEnums(schemaBuilder);
        addInterfaces(schemaBuilder);
        addInputs(schemaBuilder);
        addTypes(schemaBuilder);
        addQueries(schemaBuilder);
        addMutations(schemaBuilder);

        schemaBuilder = schemaBuilder.codeRegistry(codeRegistryBuilder.build());

        return schemaBuilder.build();
    }

    private void addEnums(GraphQLSchema.Builder schemaBuilder) {
        for (Enum enumType : schema.getEnums().values()) {

            GraphQLEnumType.Builder enumBuilder = GraphQLEnumType.newEnum()
                    .name(enumType.getName())
                    .description(enumType.getDescription());
            // Values
            for (String value : enumType.getValues()) {
                enumBuilder = enumBuilder.value(value);
            }
            schemaBuilder = schemaBuilder.additionalType(enumBuilder.build());
        }
    }

    private void addInterfaces(GraphQLSchema.Builder schemaBuilder) {
        for (Complex interfaceType : schema.getInterfaces().values()) {

            GraphQLInterfaceType.Builder interfaceTypeBuilder = GraphQLInterfaceType.newInterface()
                    .name(interfaceType.getName())
                    .description(interfaceType.getDescription());

            // Fields
            Set<Method> methods = interfaceType.getMethods();
            for (Method method : methods) {
                interfaceTypeBuilder = interfaceTypeBuilder.field(toGraphQLFieldDefinition(method));
            }
            GraphQLInterfaceType graphQLInterfaceType = interfaceTypeBuilder.build();
            // To resolve the concrete class
            codeRegistryBuilder.typeResolver(graphQLInterfaceType,
                    new InterfaceResolver(interfaceType.getClassName()));

            schemaBuilder = schemaBuilder.additionalType(graphQLInterfaceType);
        }
    }

    private void addInputs(GraphQLSchema.Builder schemaBuilder) {
        for (Complex inputType : schema.getInputs().values()) {

            GraphQLInputObjectType.Builder inputObjectTypeBuilder = GraphQLInputObjectType.newInputObject()
                    .name(inputType.getName())
                    .description(inputType.getDescription());

            // Fields
            Set<Method> methods = inputType.getMethods();
            for (Method method : methods) {
                inputObjectTypeBuilder = inputObjectTypeBuilder.field(toGraphQLInputObjectField(method));
            }

            schemaBuilder = schemaBuilder.additionalType(inputObjectTypeBuilder.build());
        }
    }

    private void addTypes(GraphQLSchema.Builder schemaBuilder) {
        for (Complex type : schema.getTypes().values()) {

            GraphQLObjectType.Builder objectTypeBuilder = GraphQLObjectType.newObject()
                    .name(type.getName())
                    .description(type.getDescription());

            // Fields
            Set<Method> methods = type.getMethods();
            for (Method method : methods) {
                objectTypeBuilder = objectTypeBuilder.field(toGraphQLFieldDefinition(method));
            }

            schemaBuilder = schemaBuilder.additionalType(objectTypeBuilder.build());
        }
    }

    private void addQueries(GraphQLSchema.Builder schemaBuilder) {

        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
                .name(QUERY)
                .description("Query root");

        Set<Complex> queries = schema.getQueries();
        for (Complex queryDefinition : queries) {

            Set<Method> methods = queryDefinition.getMethods();
            for (Method method : methods) {

                GraphQLFieldDefinition graphQLFieldDefinition = toGraphQLFieldDefinition(method);

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

                GraphQLFieldDefinition graphQLFieldDefinition = toGraphQLFieldDefinition(method);
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

    private GraphQLFieldDefinition toGraphQLFieldDefinition(Method method) {
        // Fields
        GraphQLFieldDefinition.Builder fieldBuilder = GraphQLFieldDefinition.newFieldDefinition()
                .name(method.getName())
                .description(method.getDescription());

        // Type (output)
        Field returnObject = method.getReturn();
        fieldBuilder = fieldBuilder.type(toGraphQLOutputType(returnObject));

        // Arguments (queries and mutations)
        if (method.hasParameters()) {
            List<Field> parameters = method.getParameters();
            for (Field parameter : parameters) {
                //builder.argument(argumentsHelper.toGraphQLArguments(inputTypeCreator, methodInfo));
            }
        }

        return fieldBuilder.build();
    }

    private GraphQLInputObjectField toGraphQLInputObjectField(Method method) {
        GraphQLInputObjectField.Builder inputFieldBuilder = GraphQLInputObjectField.newInputObjectField()
                .name(method.getName())
                .description(method.getDescription());

        // Type
        Field returnObject = method.getReturn();
        inputFieldBuilder = inputFieldBuilder.type(toGraphQLInputType(returnObject));

        GraphQLInputObjectField graphQLInputObjectField = inputFieldBuilder.build();

        //        codeRegistryBuilder.dataFetcher(FieldCoordinates.coordinates(name, fieldName),
        //                new AnnotatedPropertyDataFetcher(field.name(), field.type(), annotations));

        // Default value (on method)
        //builder = builder.defaultValue(maybeDefaultValue.orElse(null));

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

    private GraphQLInputType toGraphQLInputType(Field returnObject) {
        GraphQLInputType graphQLInputType = (GraphQLInputType) toGraphQLType(returnObject);

        // Mandatory
        if (returnObject.isMandatory()) {
            graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
        }
        // Collection
        if (returnObject.isCollection()) {
            // TODO: Add Mandatory for collection
            graphQLInputType = GraphQLList.list(graphQLInputType);
        }
        return graphQLInputType;
    }

    private GraphQLOutputType toGraphQLOutputType(Field returnObject) {
        GraphQLOutputType graphQLOutputType = (GraphQLOutputType) toGraphQLType(returnObject);

        // Mandatory
        if (returnObject.isMandatory()) {
            graphQLOutputType = GraphQLNonNull.nonNull(graphQLOutputType);
        }
        // Collection
        if (returnObject.isCollection()) {
            // TODO: Add Mandatory for collection
            graphQLOutputType = GraphQLList.list(graphQLOutputType);
        }
        return graphQLOutputType;
    }

    private GraphQLType toGraphQLType(Field returnObject) {
        ReferenceType type = returnObject.getType();
        switch (type) {
            case SCALAR:
                return ScalarHolder.getGraphQLScalarType(returnObject.getClassName());
            case ENUM:
                LOG.warn(">>>>>> Enum " + returnObject.getName());
            case INTERFACE:
                LOG.warn(">>>>>> Interface " + returnObject.getName());
            case TYPE:
                LOG.warn(">>>>>> Type " + returnObject.getName());
                // Throw something ?
            default:
                return Scalars.GraphQLString;
        }
    }

    private static final String QUERY = "Query";
    private static final String MUTATION = "Mutation";

}

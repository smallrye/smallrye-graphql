package io.smallrye.graphql.bootstrap;

import java.util.Set;

import org.jboss.logging.Logger;

import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.schema.model.Complex;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Bootstrap MicroProfile GraphQL
 * This create a graphql-java model from the smallrye model
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class GraphQLBootstrap {
    private static final Logger LOG = Logger.getLogger(GraphQLBootstrap.class.getName());

    private Schema schema;

    public static GraphQLSchema bootstrap(Schema schema) {
        GraphQLBootstrap graphQLBootstrap = new GraphQLBootstrap(schema);
        return graphQLBootstrap.generateGraphQLSchema();
    }

    private GraphQLBootstrap(Schema schema) {
        this.schema = schema;
    }

    private GraphQLSchema generateGraphQLSchema() {

        GraphQLObjectType rootQuery = generateRootQuery();
        GraphQLObjectType rootMutation = generateRootMutation();

        return createGraphQLSchema(rootQuery, rootMutation);
    }

    private GraphQLObjectType generateRootQuery() {
        GraphQLObjectType.Builder queryBuilder = GraphQLObjectType.newObject()
                .name("Query")
                .description("Query root");

        Set<Complex> queries = schema.getQueries();
        for (Complex queryDefinition : queries) {
            String cdiBean = queryDefinition.getClassName();
            LOG.error("CDI Bean [" + cdiBean + "]");
        }

        return queryBuilder.build();
    }

    private GraphQLObjectType generateRootMutation() {
        GraphQLObjectType.Builder mutationBuilder = GraphQLObjectType.newObject()
                .name("Mutation")
                .description("Mutation root");

        return mutationBuilder.build();
    }

    private GraphQLSchema createGraphQLSchema(GraphQLObjectType query, GraphQLObjectType mutation) {

        GraphQLSchema.Builder schemaBuilder = GraphQLSchema.newSchema();

        //        Set<GraphQLType> additionalTypes = new HashSet<>();
        //        additionalTypes.addAll(objectBag.getEnumMap().values());
        //        additionalTypes.addAll(objectBag.getTypeMap().values());
        //        additionalTypes.addAll(objectBag.getInputMap().values());
        //        additionalTypes.addAll(objectBag.getInterfaceMap().values());
        //        schemaBuilder = schemaBuilder.additionalTypes(additionalTypes);

        if (!query.getFieldDefinitions().isEmpty()) {
            schemaBuilder = schemaBuilder.query(query);
        }
        if (!mutation.getFieldDefinitions().isEmpty()) {
            schemaBuilder = schemaBuilder.mutation(mutation);
        }

        //        schemaBuilder = schemaBuilder.codeRegistry(objectBag.getCodeRegistryBuilder().build());

        return schemaBuilder.build();
    }
}

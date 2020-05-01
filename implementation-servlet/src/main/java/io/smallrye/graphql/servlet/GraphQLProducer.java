package io.smallrye.graphql.servlet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import org.eclipse.microprofile.metrics.MetricRegistry;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.metrics.MetricsService;
import io.smallrye.graphql.schema.model.Schema;

/**
 * Produces the GraphQL Schema
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLProducer {

    private GraphQLSchema graphQLSchema;
    private ExecutionService executionService;
    private SchemaPrinter schemaPrinter;

    public void initializeGraphQL(GraphQLConfig config, Schema schema) {
        this.graphQLSchema = Bootstrap.bootstrap(schema, config);
        if (config.isMetricsEnabled()) {
            MetricRegistry vendorRegistry = MetricsService.load().getMetricRegistry(MetricRegistry.Type.VENDOR);
            Bootstrap.registerMetrics(schema, vendorRegistry);
        }
        this.executionService = new ExecutionService(config, graphQLSchema);
        this.schemaPrinter = new SchemaPrinter(config);
    }

    @Produces
    public GraphQLSchema getGraphQLSchema() {
        return this.graphQLSchema;
    }

    @Produces
    public ExecutionService getExecutionService() {
        return executionService;
    }

    @Produces
    public SchemaPrinter getSchemaPrinter() {
        return schemaPrinter;
    }
}

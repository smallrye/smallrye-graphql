package io.smallrye.graphql.cdi.producer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.metrics.MetricRegistry;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.Bootstrap;
import io.smallrye.graphql.cdi.config.GraphQLConfig;
import io.smallrye.graphql.execution.ExecutionService;
import io.smallrye.graphql.execution.SchemaPrinter;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.spi.MetricsService;

/**
 * Produces the GraphQL Services
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class GraphQLProducer {

    private Schema schema;

    @Inject
    GraphQLConfig graphQLConfig;

    public void setSchema(Schema schema) {
        this.schema = schema;
    }

    public GraphQLSchema initialize(Schema schema) {
        this.schema = schema;
        return initialize();
    }

    public GraphQLSchema initialize() {
        this.graphQLSchema = Bootstrap.bootstrap(schema, graphQLConfig);
        if (graphQLConfig.isMetricsEnabled()) {
            MetricRegistry vendorRegistry = MetricsService.load().getMetricRegistry(MetricRegistry.Type.VENDOR);
            Bootstrap.registerMetrics(schema, vendorRegistry);
        }
        this.executionService = new ExecutionService(graphQLConfig, graphQLSchema);
        this.schemaPrinter = new SchemaPrinter(graphQLConfig);
        return this.graphQLSchema;
    }

    @Produces
    SchemaPrinter schemaPrinter;

    @Produces
    ExecutionService executionService;

    @Produces
    GraphQLSchema graphQLSchema;
}

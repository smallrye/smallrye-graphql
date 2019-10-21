package io.smallrye.graphql;

import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebListener;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import graphql.servlet.SimpleGraphQLHttpServlet;

/**
 * Dynamically adding the Endpoint and Schema Servlets
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebListener
public class SmallRyeGraphQLListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(SmallRyeGraphQLListener.class.getName());

    @Inject
    @ConfigProperty(name = "mp.graphql.contextpath", defaultValue = "/graphql")
    private String path;

    @Inject
    private Bootstrap bootstrap;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("=== Bootstrapping Test Runtime ===");
        GraphQLSchema schema = bootstrap.generateSchema();
        LOG.error(schema);

        ServletContext context = sce.getServletContext();

        // The Endpoint
        SimpleGraphQLHttpServlet graphQLServlet = SimpleGraphQLHttpServlet.newBuilder(schema).build();
        ServletRegistration.Dynamic endpointservlet = context.addServlet(GRAPHQL_SERVLET_NAME, graphQLServlet);
        endpointservlet.addMapping(path + SLASH_STAR);

        // The Schema
        SmallRyeGraphQLSchemaServlet schemaServlet = new SmallRyeGraphQLSchemaServlet(schemaToString(schema));
        ServletRegistration.Dynamic schemaservlet = context.addServlet(GRAPHQL_SCHEMA_SERVLET_NAME, schemaServlet);
        schemaservlet.addMapping(path + SLASH_SCHEMA_GRAPHQL);

        LOG.warn("GraphQL Endpoint available on " + path);
    }

    private String schemaToString(GraphQLSchema schema) {
        SchemaPrinter schemaPrinter = new SchemaPrinter();
        return schemaPrinter.print(schema);
    }

    private static final String GRAPHQL_SERVLET_NAME = "GraphQLServlet";
    private static final String GRAPHQL_SCHEMA_SERVLET_NAME = "GraphQLSchemaServlet";
    private static final String SLASH_STAR = "/*";
    private static final String SLASH_SCHEMA_GRAPHQL = "/schema.graphql";

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}

package io.smallrye.graphql.servlet;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.bootstrap.SmallRyeGraphQLBootstrap;
import io.smallrye.graphql.bootstrap.index.IndexInitializer;
import io.smallrye.graphql.execution.GraphQLProducer;

/**
 * Bootstrap the application on startup
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@WebListener
public class StartupListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(StartupListener.class.getName());

    @Inject
    private GraphQLProducer graphQLProducer;

    private final IndexInitializer indexInitializer = new IndexInitializer();

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            String realPath = sce.getServletContext().getRealPath("WEB-INF/classes");
            URL url = Paths.get(realPath).toUri().toURL();
            IndexView index = indexInitializer.createIndex(url);
            GraphQLSchema graphQLSchema = SmallRyeGraphQLBootstrap.bootstrap(index);
            //Schema schema = GraphQLSchemaBuilder.build(index);
            //GraphQLSchema graphQLSchema2 = GraphQLBootstrap.bootstrap(schema);

            //            LOG.error("=============== New Schema !! ===============");
            //            LOG.error(SchemaPrinter.print(graphQLSchema2));

            graphQLProducer.setGraphQLSchema(graphQLSchema);
            sce.getServletContext().setAttribute(SchemaServlet.SCHEMA_PROP, graphQLSchema);
            LOG.info("SmallRye GraphQL initialized");
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("SmallRye GraphQL destroyed");
    }
}

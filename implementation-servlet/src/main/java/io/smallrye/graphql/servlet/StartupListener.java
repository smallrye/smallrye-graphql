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

import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;

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

    @Inject
    private GraphQLConfig config;

    private final IndexInitializer indexInitializer = new IndexInitializer();

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            String realPath = sce.getServletContext().getRealPath("WEB-INF/classes");
            URL url = Paths.get(realPath).toUri().toURL();
            IndexView index = indexInitializer.createIndex(url);

            Schema schema = SchemaBuilder.build(index); // Get the smallrye schema
            graphQLProducer.initializeGraphQL(config, schema);

            sce.getServletContext().setAttribute(SchemaServlet.SCHEMA_PROP, graphQLProducer.getGraphQLSchema());
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

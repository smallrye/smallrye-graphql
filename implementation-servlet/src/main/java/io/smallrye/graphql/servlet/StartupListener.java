package io.smallrye.graphql.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import graphql.schema.GraphQLSchema;
import io.smallrye.graphql.cdi.producer.GraphQLProducer;
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

    private final IndexInitializer indexInitializer = new IndexInitializer();

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            Set<URL> warURLs = new HashSet<>();
            // Classes in the war
            String warClasses = sce.getServletContext().getRealPath("WEB-INF/classes");
            warURLs.add(Paths.get(warClasses).toUri().toURL());

            // Libs in the war
            String libs = sce.getServletContext().getRealPath("WEB-INF/lib");
            List<Path> jarsInLib = getJarsInLib(Paths.get(libs));
            warURLs.addAll(toURLs(jarsInLib));

            IndexView index = indexInitializer.createIndex(warURLs);

            Schema schema = SchemaBuilder.build(index); // Get the smallrye schema
            GraphQLSchema graphQLSchema = graphQLProducer.initialize(schema);

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

    private List<URL> toURLs(List<Path> paths) throws MalformedURLException {
        List<URL> urls = new ArrayList<>();
        for (Path path : paths) {
            urls.add(path.toUri().toURL());
        }
        return urls;
    }

    private List<Path> getJarsInLib(Path libFolder) {
        List<Path> jars = new ArrayList<>();
        if (libFolder != null && Files.isDirectory(libFolder)) {
            try {
                jars.addAll(Files.walk(libFolder)
                        .filter(Files::isRegularFile)
                        .collect(Collectors.toList()));
            } catch (IOException ex) {
                LOG.warn("Error reading jars files in WEB-INF/lib - not scanning libs [" + ex.getMessage() + "]");
            }
        }
        return jars;
    }
}

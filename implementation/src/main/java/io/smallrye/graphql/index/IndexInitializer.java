package io.smallrye.graphql.index;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.logging.Level;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;

/**
 * Create a index of all classes needed
 * TODO: Load from index file if this has been done on build
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class IndexInitializer {
    private static final Logger LOG = Logger.getLogger(IndexInitializer.class.getName());

    @Produces
    private Index index;

    @Inject
    private BeanManager beanManager;

    public void init(@Priority(Integer.MAX_VALUE - 2) @Observes @Initialized(ApplicationScoped.class) Object init) {
        // TODO: Use a facory to load scanners ?
        // TODO: Check if IndexInitializer is already create on build time, then load from there.
        Indexer indexer = new Indexer();
        LOG.debug("==== Now indexing all beans ====");

        Set<Bean<?>> beans = beanManager.getBeans(Object.class, new AnnotationLiteral<Any>() {
        });

        for (Bean<?> bean : beans) {

            Class clazz = bean.getBeanClass();
            // TODO: Find a nice way to do this ?
            //if (!Blacklist.ignore(clazz.getName())) {
            String className = clazz.getName().replace(DOT, SLASH) + DOT_CLASS;
            LOG.debug("indexing [" + className + "]");
            InputStream stream = clazz.getClassLoader().getResourceAsStream(className);
            try {
                indexer.index(stream);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(IndexInitializer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        this.index = indexer.complete();
    }

    private static final String DOT_CLASS = ".class";
    private static final String DOT = ".";
    private static final String SLASH = "/";

}

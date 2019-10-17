package io.smallrye.graphql;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.logging.Logger;

/**
 * Bootstrap the MicroProfile GraphQL Runtime
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class TestRuntime {
    private static final Logger LOG = Logger.getLogger(TestRuntime.class.getName());

    @Inject
    private Bootstrap bootstrap;

    public void init(@Priority(Integer.MAX_VALUE) @Observes @Initialized(ApplicationScoped.class) Object init) {
        LOG.info("=== Bootstrapping Test Runtime ===");
        bootstrap.bootstrap();
    }

}

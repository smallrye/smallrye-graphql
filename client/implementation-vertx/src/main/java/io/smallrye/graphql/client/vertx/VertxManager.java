package io.smallrye.graphql.client.vertx;

import org.jboss.logging.Logger;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.file.FileSystemOptions;
import io.vertx.core.spi.resolver.ResolverProvider;

public class VertxManager {

    private static volatile Vertx globalInstance;
    private static volatile Vertx customInstance;

    private static final Logger log = Logger.getLogger(VertxManager.class);

    /**
     * The priority where to obtain a Vert.x instance:
     * 1. The instance passed by the user to the client builder (not handled by this class)
     * 2. The global Vertx instance that was set by calling `setFromGlobal`
     * 3. Attempt to locate Vertx using the current thread context
     * 4. Create our own custom instance (in case that multiple clients fall
     * through into here, use only one instance for all)
     */
    public static Vertx get() {
        // case 2
        if (globalInstance != null) {
            log.debug("Using the global Vert.x instance");
            return globalInstance;
        }

        // case 3
        Vertx fromContext = getFromContext();
        if (fromContext != null) {
            log.debug("Using Vert.x instance " + fromContext.toString() + " found in the context");
            return fromContext;
        }

        // case 4
        return getOrCreateCustom();
    }

    public static void setFromGlobal(Vertx vertx) {
        globalInstance = vertx;
    }

    public static Future<Void> closeCustomInstance() {
        if (customInstance != null) {
            return customInstance.close();
        } else {
            return Future.succeededFuture();
        }
    }

    private static Vertx getFromContext() {
        Context vertxContext = Vertx.currentContext();
        if (vertxContext != null && vertxContext.owner() != null) {
            return vertxContext.owner();
        } else {
            return null;
        }
    }

    private static Vertx getOrCreateCustom() {
        if (customInstance == null) {
            synchronized (VertxManager.class) {
                if (customInstance == null) {
                    // when creating our own vert.x instance, we don't allow
                    // explicitly configuring it, but it should be reasonable
                    // to disable caching and the async DNS resolver
                    if (!System.getProperties().contains(ResolverProvider.DISABLE_DNS_RESOLVER_PROP_NAME)) {
                        System.setProperty(ResolverProvider.DISABLE_DNS_RESOLVER_PROP_NAME, "true");
                    }
                    FileSystemOptions fso = new FileSystemOptions();
                    fso.setFileCachingEnabled(false);
                    VertxOptions options = new VertxOptions();
                    options.setFileSystemOptions(fso);
                    customInstance = Vertx.vertx(options);
                }
            }
        }
        log.debug("Using custom Vert.x instance " + customInstance.toString());
        return customInstance;
    }

}

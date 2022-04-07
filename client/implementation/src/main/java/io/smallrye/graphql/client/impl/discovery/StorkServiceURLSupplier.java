package io.smallrye.graphql.client.impl.discovery;

import java.net.URI;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import io.smallrye.stork.Stork;

public class StorkServiceURLSupplier implements ServiceURLSupplier {

    private final Stork stork;
    private final String serviceName;
    private final String scheme;
    private final String path;
    private final Logger LOG = Logger.getLogger(StorkServiceURLSupplier.class);

    public StorkServiceURLSupplier(URI uri, boolean websocket) {
        if (uri == null || (!uri.getScheme().equals("stork") && !uri.getScheme().equals("storks"))) {
            throw new IllegalArgumentException("Not a stork URI: " + uri);
        }
        try {
            stork = Stork.getInstance();
        } catch (NoClassDefFoundError ncdfe) {
            throw new IllegalStateException("Cannot use URI " + uri + " because SmallRye Stork dependency seems to be missing",
                    ncdfe);
        }

        this.serviceName = uri.getHost();
        if (uri.getScheme().equals("storks")) {
            if (websocket) {
                scheme = "wss";
            } else {
                scheme = "https";
            }
        } else {
            if (websocket) {
                scheme = "ws";
            } else {
                scheme = "http";
            }
        }
        this.path = uri.getPath() + (uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery());
    }

    @Override
    public Uni<String> get() {
        return stork.getService(serviceName).selectInstance().onItem().transform(i -> {
            String s = scheme + "://" + i.getHost() + ":" + i.getPort() + path;
            if (LOG.isTraceEnabled()) {
                LOG.trace("Using URI: " + s);
            }
            return s;
        });
    }
}

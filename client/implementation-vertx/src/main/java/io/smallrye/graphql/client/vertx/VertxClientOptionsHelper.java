package io.smallrye.graphql.client.vertx;

import java.security.KeyStore;

import io.smallrye.graphql.client.impl.GraphQLClientConfiguration;
import io.smallrye.graphql.client.vertx.ssl.SSLTools;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.ProxyOptions;

public class VertxClientOptionsHelper {

    public static void applyConfigToVertxOptions(HttpClientOptions options, GraphQLClientConfiguration configuration) {
        if (options.getTrustStoreOptions() == null && configuration.getTrustStore() != null) {
            options.setSsl(true);
            JksOptions trustStoreOptions = new JksOptions();
            KeyStore trustStore = SSLTools.createKeyStore(configuration.getTrustStore(),
                    configuration.getTrustStoreType(),
                    configuration.getTrustStorePassword());
            trustStoreOptions.setValue(SSLTools.asBuffer(trustStore, configuration.getTrustStorePassword().toCharArray()));
            trustStoreOptions.setPassword(new String(configuration.getTrustStorePassword()));
            options.setTrustStoreOptions(trustStoreOptions);
        }

        if (options.getKeyStoreOptions() == null && configuration.getKeyStore() != null) {
            options.setSsl(true);
            JksOptions keyStoreOptions = new JksOptions();
            KeyStore keyStore = SSLTools.createKeyStore(configuration.getKeyStore(),
                    configuration.getKeyStoreType(),
                    configuration.getKeyStorePassword());
            keyStoreOptions.setValue(SSLTools.asBuffer(keyStore, configuration.getKeyStorePassword().toCharArray()));
            keyStoreOptions.setPassword(new String(configuration.getKeyStorePassword()));
            options.setKeyStoreOptions(keyStoreOptions);
        }

        if (options.getProxyOptions() == null && configuration.getProxyHost() != null) {
            ProxyOptions proxyOptions = new ProxyOptions();
            proxyOptions.setHost(configuration.getProxyHost());
            proxyOptions.setPort(configuration.getProxyPort());
            proxyOptions.setUsername(configuration.getProxyUsername());
            proxyOptions.setPassword(configuration.getProxyPassword());
            options.setProxyOptions(proxyOptions);
        }

        if (configuration.getMaxRedirects() != null) {
            options.setMaxRedirects(configuration.getMaxRedirects());
        }

        if (options.isSsl()) {
            // TODO: this is not supported yet
            options.setVerifyHost(false);
        }
    }
}

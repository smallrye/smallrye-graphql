package io.smallrye.graphql.client.vertx;

import java.security.KeyStore;

import io.smallrye.graphql.client.impl.GraphQLClientConfiguration;
import io.smallrye.graphql.client.vertx.ssl.SSLTools;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.net.JksOptions;
import io.vertx.core.net.KeyCertOptions;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.core.net.SSLOptions;
import io.vertx.core.net.TrustOptions;

public class VertxClientOptionsHelper {

    public static void applyConfigToVertxOptions(HttpClientOptions options, GraphQLClientConfiguration configuration) {
        configure(options, configuration);
        TrustOptions tlsTrustStoreOptions = (TrustOptions) configuration.getTlsTrustStoreOptions();
        KeyCertOptions tlsKeyStoreOptions = (KeyCertOptions) configuration.getTlsKeyStoreOptions();
        if (tlsTrustStoreOptions != null) {
            options.setSsl(true);
            options.setTrustOptions(tlsTrustStoreOptions);
        } else if (options.getTrustStoreOptions() == null && configuration.getTrustStore() != null) { // deprecated in Quarkus
            options.setSsl(true);
            JksOptions trustStoreOptions = new JksOptions();
            KeyStore trustStore = SSLTools.createKeyStore(configuration.getTrustStore(),
                    configuration.getTrustStoreType(),
                    configuration.getTrustStorePassword());
            trustStoreOptions.setValue(SSLTools.asBuffer(trustStore, configuration.getTrustStorePassword().toCharArray()));
            trustStoreOptions.setPassword(new String(configuration.getTrustStorePassword()));
            options.setTrustStoreOptions(trustStoreOptions);
        }
        if (tlsKeyStoreOptions != null) {
            options.setSsl(true);
            options.setKeyCertOptions(tlsKeyStoreOptions);
        } else if (options.getKeyStoreOptions() == null && configuration.getKeyStore() != null) { // deprecated in Quarkus
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
            proxyOptions.setType(toVertxProxyType(configuration.getProxyType()));
            if (configuration.getNonProxyHosts() != null) {
                for (String nonProxyHost : configuration.getNonProxyHosts()) {
                    options.addNonProxyHost(nonProxyHost);
                }
            }
            options.setProxyOptions(proxyOptions);
        }

        if (configuration.getMaxRedirects() != null) {
            options.setMaxRedirects(configuration.getMaxRedirects());
        }
    }

    private static ProxyType toVertxProxyType(GraphQLClientConfiguration.ProxyType proxyType) {
        if (proxyType == null) {
            return ProxyType.HTTP;
        }
        return switch (proxyType) {
            case HTTP -> ProxyType.HTTP;
            case SOCKS4 -> ProxyType.SOCKS4;
            case SOCKS5 -> ProxyType.SOCKS5;
        };
    }

    private static void configure(HttpClientOptions options, GraphQLClientConfiguration graphQLClientConfiguration) {
        options.setForceSni(graphQLClientConfiguration.usesSni() != null && graphQLClientConfiguration.usesSni());
        if (graphQLClientConfiguration.getHostnameVerificationAlgorithm() == null
                || graphQLClientConfiguration.getHostnameVerificationAlgorithm().equals("NONE")) {
            options.setVerifyHost(false);
        }
        SSLOptions sslOptions = (SSLOptions) graphQLClientConfiguration.getSslOptions();
        if (sslOptions != null) {
            options.setSslHandshakeTimeout(sslOptions.getSslHandshakeTimeout());
            options.setSslHandshakeTimeoutUnit(sslOptions.getSslHandshakeTimeoutUnit());
            for (String suite : sslOptions.getEnabledCipherSuites()) {
                options.addEnabledCipherSuite(suite);
            }
            for (Buffer buffer : sslOptions.getCrlValues()) {
                options.addCrlValue(buffer);
            }
            options.setEnabledSecureTransportProtocols(sslOptions.getEnabledSecureTransportProtocols());
            options.setUseAlpn(sslOptions.isUseAlpn());
        }
    }
}

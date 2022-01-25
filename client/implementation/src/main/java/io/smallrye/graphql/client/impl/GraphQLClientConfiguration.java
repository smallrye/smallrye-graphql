package io.smallrye.graphql.client.impl;

import java.util.List;
import java.util.Map;

/**
 * The configuration of a single GraphQL client.
 */
public class GraphQLClientConfiguration {

    /**
     * The URL that the client connects to.
     */
    private String url;

    /**
     * HTTP headers to be appended to each HTTP request.
     */
    private Map<String, String> headers;

    /**
     * Names of websocket subprotocols that this client will understand. The actual subprotocol to be used
     * will be subject to negotiation with the server.
     */
    private List<String> websocketSubprotocols;

    /**
     * Path to the trust store. Can point to either a classpath resource or a file.
     */
    private String trustStore;

    /**
     * The trust store password.
     */
    private String trustStorePassword;

    /**
     * The type of the trust store. Defaults to "JKS".
     */
    private String trustStoreType;

    /**
     * Path to the key store. Can point to either a classpath resource or a file.
     */
    private String keyStore;

    /**
     * The key store password.
     */
    private String keyStorePassword;

    /**
     * The type of the key store. Defaults to "JKS".
     */
    private String keyStoreType;

    /**
     * Hostname of the proxy to use.
     */
    private String proxyHost;

    /**
     * Port number of the proxy to use.
     */
    private Integer proxyPort;

    /**
     * Username for the proxy to use.
     */
    private String proxyUsername;

    /**
     * Password for the proxy to use.
     */
    private String proxyPassword;

    /**
     * Maximum number of redirects to follow.
     */
    private Integer maxRedirects;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public List<String> getWebsocketSubprotocols() {
        return websocketSubprotocols;
    }

    public void setWebsocketSubprotocols(List<String> websocketSubprotocols) {
        this.websocketSubprotocols = websocketSubprotocols;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public Integer getMaxRedirects() {
        return maxRedirects;
    }

    public void setMaxRedirects(Integer maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    /**
     * Merge the `other` configuration into this one. Values in `other` take precedence.
     * This method has to be idempotent because it can be called multiple times to allow for changes in configuration.
     */
    public GraphQLClientConfiguration merge(GraphQLClientConfiguration other) {
        if (other.url != null) {
            this.url = other.url;
        }
        if (this.headers == null) {
            this.headers = other.headers;
        } else if (other.headers != null) {
            other.headers.forEach((key, value) -> this.headers.put(key, value));
        }
        if (this.websocketSubprotocols == null) {
            this.websocketSubprotocols = other.websocketSubprotocols;
        } else if (other.websocketSubprotocols != null) {
            for (String subprotocol : other.websocketSubprotocols) {
                if (!this.websocketSubprotocols.contains(subprotocol)) {
                    this.websocketSubprotocols.add(subprotocol);
                }
            }
        }
        if (other.trustStore != null) {
            this.trustStore = other.trustStore;
        }
        if (other.trustStorePassword != null) {
            this.trustStorePassword = other.trustStorePassword;
        }
        if (other.trustStoreType != null) {
            this.trustStoreType = other.trustStoreType;
        }
        if (other.keyStore != null) {
            this.keyStore = other.keyStore;
        }
        if (other.keyStorePassword != null) {
            this.keyStorePassword = other.keyStorePassword;
        }
        if (other.keyStoreType != null) {
            this.keyStoreType = other.keyStoreType;
        }
        if (other.proxyHost != null) {
            this.proxyHost = other.proxyHost;
        }
        if (other.proxyPort != null && other.proxyPort != 0) {
            this.proxyPort = other.proxyPort;
        }
        if (other.proxyUsername != null) {
            this.proxyUsername = other.proxyUsername;
        }
        if (other.proxyPassword != null) {
            this.proxyPassword = other.proxyPassword;
        }
        if (other.maxRedirects != null) {
            this.maxRedirects = other.maxRedirects;
        }
        return this;
    }
}

package io.smallrye.graphql.client.impl;

import java.util.List;
import java.util.Map;

import io.smallrye.mutiny.Uni;

/**
 * The configuration of a single GraphQL client.
 */
public class GraphQLClientConfiguration {

    /**
     * The URL that the client connects to.
     */
    private String url;

    /**
     * The WebSocket URL that the client connects to. By default, this is the HTTP url with the protocol part changed to `ws`.
     */
    private String websocketUrl;

    /**
     * HTTP headers to be appended to each HTTP request.
     */
    private Map<String, String> headers;

    /**
     * HTTP headers that need to be resolved dynamically for each request.
     */
    private Map<String, Uni<String>> dynamicHeaders;

    /**
     * Additional payload sent on subscription initialization.
     */
    private Map<String, Object> initPayload;

    /**
     * Names of websocket subprotocols that this client will understand. The actual subprotocol to be used
     * will be subject to negotiation with the server.
     */
    private List<String> websocketSubprotocols;

    /**
     * If this is true, then queries and mutations will also be executed over a websocket connection rather than over pure HTTP.
     * As this comes with higher overhead, it is false by default.
     */
    private Boolean executeSingleOperationsOverWebsocket;

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
     * The key store options, already contains key store.
     */
    private Object tlsKeyStoreOptions;

    /**
     * The trust store options, already contains trust store.
     */
    private Object tlsTrustStoreOptions;

    /**
     * SSL options for connection.
     */
    private Object sslOptions;

    /**
     * Indicates whether Server Name Indication (SNI) is enabled for this connection.
     * When SNI is enabled, the client sends the server name during the TLS handshake,
     * allowing the server to select the appropriate certificate based on the hostname.
     */
    private Boolean usesSni;

    /**
     * Specifies the algorithm used for hostname verification during the TLS handshake.
     */
    private String hostnameVerificationAlgorithm;

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
     * Type of proxy to use. The default is HTTP.
     */
    private ProxyType proxyType;

    private List<String> nonProxyHosts;

    /**
     * Maximum number of redirects to follow.
     */
    private Integer maxRedirects;

    /**
     * Maximum time in milliseconds that will be allowed to wait for the server to acknowledge a websocket connection.
     */
    private Integer websocketInitializationTimeout;

    /**
     * If true and there is an unexpected field in the response payload, it won't throw an error, and it will log
     * a warning message.
     */
    private Boolean allowUnexpectedResponseFields;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getWebsocketUrl() {
        return websocketUrl;
    }

    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, Uni<String>> getDynamicHeaders() {
        return dynamicHeaders;
    }

    public void setDynamicHeaders(Map<String, Uni<String>> dynamicHeaders) {
        this.dynamicHeaders = dynamicHeaders;
    }

    public Map<String, Object> getInitPayload() {
        return initPayload;
    }

    public void setInitPayload(Map<String, Object> initPayload) {
        this.initPayload = initPayload;
    }

    public List<String> getWebsocketSubprotocols() {
        return websocketSubprotocols;
    }

    public void setWebsocketSubprotocols(List<String> websocketSubprotocols) {
        this.websocketSubprotocols = websocketSubprotocols;
    }

    public Boolean getExecuteSingleOperationsOverWebsocket() {
        return executeSingleOperationsOverWebsocket;
    }

    public void setExecuteSingleOperationsOverWebsocket(Boolean executeSingleOperationsOverWebsocket) {
        this.executeSingleOperationsOverWebsocket = executeSingleOperationsOverWebsocket;
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

    public Integer getWebsocketInitializationTimeout() {
        return websocketInitializationTimeout;
    }

    public void setWebsocketInitializationTimeout(Integer websocketInitializationTimeout) {
        this.websocketInitializationTimeout = websocketInitializationTimeout;
    }

    public Boolean getAllowUnexpectedResponseFields() {
        return allowUnexpectedResponseFields;
    }

    public void setAllowUnexpectedResponseFields(Boolean allowUnexpectedResponseFields) {
        this.allowUnexpectedResponseFields = allowUnexpectedResponseFields;
    }

    public Object getTlsKeyStoreOptions() {
        return tlsKeyStoreOptions;
    }

    public void setTlsKeyStoreOptions(Object tlsKeyStoreOptions) {
        this.tlsKeyStoreOptions = tlsKeyStoreOptions;
    }

    public Object getTlsTrustStoreOptions() {
        return tlsTrustStoreOptions;
    }

    public void setTlsTrustStoreOptions(Object tlsTrustStoreOptions) {
        this.tlsTrustStoreOptions = tlsTrustStoreOptions;
    }

    public Boolean usesSni() {
        return usesSni;
    }

    public void setUsesSni(Boolean usesSni) {
        this.usesSni = usesSni;
    }

    public String getHostnameVerificationAlgorithm() {
        return hostnameVerificationAlgorithm;
    }

    public void setHostnameVerificationAlgorithm(String hostnameVerificationAlgorithm) {
        this.hostnameVerificationAlgorithm = hostnameVerificationAlgorithm;
    }

    public Object getSslOptions() {
        return sslOptions;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }

    public void setProxyType(ProxyType proxyType) {
        this.proxyType = proxyType;
    }

    public List<String> getNonProxyHosts() {
        return nonProxyHosts;
    }

    public void setNonProxyHosts(List<String> nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

    public void setSslOptions(Object sslOptions) {
        this.sslOptions = sslOptions;
    }

    /**
     * Merge the `other` configuration into this one. Values in `other` take precedence.
     * This method has to be idempotent because it can be called multiple times to allow for changes in configuration.
     */
    public GraphQLClientConfiguration merge(GraphQLClientConfiguration other) {
        if (other.url != null) {
            this.url = other.url;
        }
        if (other.websocketUrl != null) {
            this.websocketUrl = other.websocketUrl;
        }
        if (this.headers == null) {
            this.headers = other.headers;
        } else if (other.headers != null) {
            other.headers.forEach((key, value) -> this.headers.put(key, value));
        }
        if (this.dynamicHeaders == null) {
            this.dynamicHeaders = other.dynamicHeaders;
        } else if (other.dynamicHeaders != null) {
            other.dynamicHeaders.forEach((key, value) -> this.dynamicHeaders.put(key, value));
        }
        if (this.initPayload == null) {
            this.initPayload = other.initPayload;
        } else if (other.initPayload != null) {
            other.initPayload.forEach((key, value) -> this.initPayload.put(key, value));
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
        if (this.executeSingleOperationsOverWebsocket == null) {
            this.executeSingleOperationsOverWebsocket = other.executeSingleOperationsOverWebsocket;
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
        if (other.proxyType != null) {
            this.proxyType = other.proxyType;
        }
        if (other.maxRedirects != null) {
            this.maxRedirects = other.maxRedirects;
        }
        if (other.websocketInitializationTimeout != null) {
            this.websocketInitializationTimeout = other.websocketInitializationTimeout;
        }
        if (other.allowUnexpectedResponseFields != null) {
            this.allowUnexpectedResponseFields = other.allowUnexpectedResponseFields;
        }
        if (other.tlsKeyStoreOptions != null) {
            this.tlsKeyStoreOptions = other.tlsKeyStoreOptions;
        }
        if (other.tlsTrustStoreOptions != null) {
            this.tlsTrustStoreOptions = other.tlsTrustStoreOptions;
        }
        if (other.usesSni != null) {
            this.usesSni = other.usesSni;
        }
        if (other.hostnameVerificationAlgorithm != null) {
            this.hostnameVerificationAlgorithm = other.hostnameVerificationAlgorithm;
        }
        if (other.sslOptions != null) {
            this.sslOptions = other.sslOptions;
        }
        return this;
    }

    public enum ProxyType {
        HTTP,
        SOCKS4,
        SOCKS5
    }

}

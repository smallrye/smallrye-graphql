package io.smallrye.graphql.client.impl.typesafe;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.graphql.client.GraphQLClientException;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodInvocation;
import io.smallrye.graphql.client.impl.typesafe.reflection.MethodResolver;
import io.smallrye.graphql.client.impl.typesafe.reflection.ParameterInfo;
import io.smallrye.graphql.client.impl.typesafe.reflection.TypeInfo;
import io.smallrye.graphql.client.typesafe.api.AuthorizationHeader;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.Header;

public class HeaderBuilder {
    private static final String APPLICATION_JSON_UTF8 = "application/json;charset=utf-8";

    private final Class<?> api;
    private final MethodInvocation method;
    private final Map<String, String> additionalHeaders;

    private final String configKey;
    private final Config config = ConfigProvider.getConfig();

    public HeaderBuilder(Class<?> api, MethodInvocation method, Map<String, String> additionalHeaders) {
        this.api = api;
        this.method = method;
        this.additionalHeaders = additionalHeaders;

        this.configKey = configKey();
    }

    private String configKey() {
        GraphQLClientApi annotation = api.getAnnotation(GraphQLClientApi.class);
        if (annotation == null || annotation.configKey().isEmpty())
            return api.getName();
        return annotation.configKey();
    }

    public Map<String, String> build() {
        Map<String, String> headers = new LinkedHashMap<>();
        addDefaultHeaders(headers);
        if (method != null) {
            method.getResolvedAnnotations(api, Header.class)
                    // getResolvedAnnotations returns class-level annotations first
                    // so if there is something on class level, it will be overwritten
                    // by a header on the method
                    .forEach(annotation -> resolve(annotation).apply(headers));
            method.headerParameters().forEach(parameter -> resolve(parameter).apply(headers));
            method.getResolvedAnnotations(api, AuthorizationHeader.class)
                    // getResolvedAnnotations returns class-level annotations first, then method-level annotations,
                    // so we need to take the last element of this stream.
                    // This `reduce` operation is basically 'find the last element'
                    .reduce((first, second) -> second)
                    .map(this::resolveAuthHeader)
                    .ifPresent(auth -> headers.put("Authorization", auth));
        }
        if (additionalHeaders != null) {
            headers.putAll(additionalHeaders);
        }
        headers.putAll(configuredCredentials());
        return headers;
    }

    private void addDefaultHeaders(Map<String, String> headers) {
        headers.put("Accept", APPLICATION_JSON_UTF8);
        headers.put("Content-Type", APPLICATION_JSON_UTF8);
    }

    private HeaderDescriptor resolve(Header header) {
        if (!header.method().isEmpty()) {
            if (!header.constant().isEmpty())
                throw new RuntimeException("Header with 'method' AND 'constant' not allowed: " + header);
            return resolveHeaderMethod(header);
        }
        if (header.constant().isEmpty())
            throw new RuntimeException("Header must have either 'method' XOR 'constant': " + header);
        if (header.name().isEmpty())
            throw new RuntimeException("Missing header name for constant '" + header.constant() + "'");
        return new HeaderDescriptor(header.constant(), header.name(), null);
    }

    private HeaderDescriptor resolveHeaderMethod(Header header) {
        TypeInfo declaringType = method.getDeclaringType();
        MethodInvocation method = new MethodResolver(declaringType, header.method()).resolve();
        if (!method.isStatic())
            throw new RuntimeException("referenced header method '" + header.method() + "'" +
                    " in " + declaringType.getTypeName() + " is not static");
        String value = callMethod(method);
        return new HeaderDescriptor(value, header.name(), toHeaderName(method));
    }

    private String callMethod(MethodInvocation method) {
        try {
            Object result = method.invoke(null);
            return (result == null) ? null : result.toString();
        } catch (GraphQLClientException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new RuntimeException("can't resolve header value from method " + method, e);
        }
    }

    private String toHeaderName(MethodInvocation method) {
        String name = method.getName();
        return method.isRenamed() ? name : camelToKebab(name);
    }

    private HeaderDescriptor resolve(ParameterInfo parameter) {
        Header header = parameter.getAnnotations(Header.class)[0];
        return new HeaderDescriptor(parameter.getValue(), header.name(), toHeaderName(parameter));
    }

    private String toHeaderName(ParameterInfo parameter) {
        String name = parameter.getName();
        return parameter.isRenamed() ? name : camelToKebab(name);
    }

    private static String camelToKebab(String input) {
        return String.join("-", input.split("(?=\\p{javaUpperCase})")); // header title-casing is done by vert.x
    }

    private static class HeaderDescriptor {
        private final String name;
        private final String value;

        public HeaderDescriptor(Object value, String name, String fallbackName) {
            this.value = (value == null) ? null : value.toString();
            this.name = (name.isEmpty()) ? fallbackName : name;
        }

        public void apply(Map<String, String> headers) {
            if (value != null) {
                headers.put(name, value);
            }
        }
    }

    private String resolveAuthHeader(AuthorizationHeader header) {
        String prefix = prefix(header);
        switch (header.type()) {
            case BASIC:
                return basic(prefix);
            case BEARER:
                return bearer(prefix);
        }
        throw new UnsupportedOperationException("unreachable");
    }

    private String prefix(AuthorizationHeader header) {
        String key = header.confPrefix().isEmpty() ? this.configKey : header.confPrefix();
        if (key.endsWith("*"))
            return key.substring(0, key.length() - 1);
        else
            return key + "/mp-graphql/";
    }

    private String basic(String prefix) {
        String username = config.getValue(prefix + "username", String.class);
        String password = config.getValue(prefix + "password", String.class);
        return basic(username, password);
    }

    private String basic(String username, String password) {
        String token = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(UTF_8));
    }

    private String bearer(String prefix) {
        return "Bearer " + config.getValue(prefix + "bearer", String.class);
    }

    private Map<String, String> configuredCredentials() {
        Optional<String> username = config.getOptionalValue(this.configKey + "/mp-graphql/username", String.class);
        Optional<String> password = config.getOptionalValue(this.configKey + "/mp-graphql/password", String.class);
        return username.isPresent() && password.isPresent()
                ? singletonMap("Authorization", basic(username.get(), password.get()))
                : emptyMap();
    }
}

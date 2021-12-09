package io.smallrye.graphql.client.vertx.typesafe;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;
import java.util.Map;

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
import io.vertx.core.MultiMap;
import io.vertx.core.http.impl.headers.HeadersMultiMap;

public class HeaderBuilder {
    private final Class<?> api;
    private final MethodInvocation method;
    private final Map<String, String> additionalHeaders;

    public HeaderBuilder(Class<?> api, MethodInvocation method, Map<String, String> additionalHeaders) {
        this.api = api;
        this.method = method;
        this.additionalHeaders = additionalHeaders;
    }

    public MultiMap build() {
        MultiMap headers = new HeadersMultiMap();
        // getResolvedAnnotations returns class-level annotations first
        // so if there is something on class level, it will be overwritten
        // by a header on the method
        method.getResolvedAnnotations(api, Header.class)
                .forEach(annotation -> resolve(annotation).apply(headers));
        method.headerParameters().forEach(parameter -> resolve(parameter).apply(headers));
        method.getResolvedAnnotations(api, AuthorizationHeader.class)
                // getResolvedAnnotations returns class-level annotations first, then method-level annotations
                // so we need to take the last element of this stream.
                // This reduce operation is basically 'find the last element'
                .reduce((first, second) -> second)
                .map(header -> resolveAuthHeader(method.getDeclaringType(), header))
                .ifPresent(auth -> headers.set("Authorization", auth));
        if (additionalHeaders != null) {
            additionalHeaders.forEach(headers::set);
        }
        return headers;
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

        public void apply(MultiMap headers) {
            if (value != null) {
                headers.set(name, value);
            }
        }
    }

    private String resolveAuthHeader(TypeInfo declaringType, AuthorizationHeader header) {
        if (header.confPrefix().isEmpty())
            return auth(header.type(), declaringType.getRawType());
        return auth(header.type(), header.confPrefix());
    }

    private static String auth(AuthorizationHeader.Type type, Class<?> api) {
        return auth(type, configKey(api));
    }

    private static String configKey(Class<?> api) {
        GraphQLClientApi annotation = api.getAnnotation(GraphQLClientApi.class);
        if (annotation == null || annotation.configKey().isEmpty())
            return api.getName();
        return annotation.configKey();
    }

    private static String auth(AuthorizationHeader.Type type, String configKey) {
        String prefix;
        if (configKey.endsWith("*"))
            prefix = configKey.substring(0, configKey.length() - 1);
        else
            prefix = configKey + "/mp-graphql/";
        switch (type) {
            case BASIC:
                return basic(prefix);
            case BEARER:
                return bearer(prefix);
        }
        throw new UnsupportedOperationException("unreachable");
    }

    private static String basic(String prefix) {
        String username = CONFIG.getValue(prefix + "username", String.class);
        String password = CONFIG.getValue(prefix + "password", String.class);
        String token = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(UTF_8));
    }

    private static String bearer(String prefix) {
        return "Bearer " + CONFIG.getValue(prefix + "bearer", String.class);
    }

    private static final Config CONFIG = ConfigProvider.getConfig();
}

package io.smallrye.graphql.client.typesafe.vertx;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;
import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.graphql.client.typesafe.api.AuthorizationHeader;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientException;
import io.smallrye.graphql.client.typesafe.api.Header;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInvocation;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodResolver;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;
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
        method.getResolvedAnnotations(api, Header.class)
                .forEach(e -> {
                    // getResolvedAnnotations returns class-level annotations first
                    // so if there is something on class level, it will be overwritten
                    // by a header on the method
                    headers.set(e.name(), resolveValue(e));
                });
        method.headerParameters().forEach(parameter -> {
            Header header = parameter.getAnnotations(Header.class)[0];
            headers.set(header.name(), (String) parameter.getValue());
        });
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

    private String resolveValue(Header header) {
        if (!header.method().isEmpty()) {
            if (!header.constant().isEmpty())
                throw new GraphQLClientException("Header with 'method' AND 'constant' not allowed: " + header);
            return resolveMethodValue(header.method());
        }
        if (header.constant().isEmpty())
            throw new GraphQLClientException("Header must have either 'method' XOR 'constant': " + header);
        return header.constant();
    }

    private String resolveMethodValue(String methodName) {
        TypeInfo declaringType = method.getDeclaringType();
        MethodInvocation method = new MethodResolver(declaringType, methodName).resolve();
        if (!method.isStatic())
            throw new GraphQLClientException("referenced header method '" + methodName + "'" +
                    " in " + declaringType.getTypeName() + " is not static");
        try {
            return method.invoke(null).toString();
        } catch (RuntimeException e) {
            if (e instanceof GraphQLClientException)
                throw e;
            throw new GraphQLClientException("can't resolve header method expression '" + methodName + "'" +
                    " in " + declaringType.getTypeName(), e);
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

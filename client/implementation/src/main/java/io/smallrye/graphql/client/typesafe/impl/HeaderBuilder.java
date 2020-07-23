package io.smallrye.graphql.client.typesafe.impl;

import static io.smallrye.graphql.client.typesafe.impl.CollectionUtils.toMultivaluedMap;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.AbstractMap.SimpleEntry;
import java.util.Base64;

import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.graphql.client.typesafe.api.AuthorizationHeader;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;
import io.smallrye.graphql.client.typesafe.api.Header;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodInfo;
import io.smallrye.graphql.client.typesafe.impl.reflection.MethodResolver;
import io.smallrye.graphql.client.typesafe.impl.reflection.TypeInfo;

public class HeaderBuilder {
    private final Class<?> api;
    private final MethodInfo method;

    public HeaderBuilder(Class<?> api, MethodInfo method) {
        this.api = api;
        this.method = method;
    }

    public MultivaluedMap<String, Object> build() {
        MultivaluedMap<String, Object> headers = method.getResolvedAnnotations(api, Header.class)
                .map(header -> new SimpleEntry<>(header.name(), resolveValue(header)))
                .collect(toMultivaluedMap());
        method.parameters()
                .filter(parameter -> parameter.isAnnotated(Header.class))
                .forEach(parameter -> {
                    Header header = parameter.getAnnotations(Header.class)[0];
                    headers.add(header.name(), parameter.getValue());
                });
        method.getResolvedAnnotations(api, AuthorizationHeader.class)
                .findFirst()
                .map(header -> resolveAuthHeader(method.getDeclaringType(), header))
                .ifPresent(auth -> headers.add("Authorization", auth));
        return headers;
    }

    private Object resolveValue(Header header) {
        if (!header.method().isEmpty()) {
            if (!header.constant().isEmpty())
                throw new GraphQlClientException("Header with 'method' AND 'constant' not allowed: " + header);
            return resolveMethodValue(header.method());
        }
        if (header.constant().isEmpty())
            throw new GraphQlClientException("Header must have either 'method' XOR 'constant': " + header);
        return header.constant();
    }

    private Object resolveMethodValue(String methodName) {
        TypeInfo declaringType = method.getDeclaringType();
        MethodInfo method = new MethodResolver(declaringType, methodName).resolve();
        if (!method.isStatic())
            throw new GraphQlClientException("referenced header method '" + methodName + "'" +
                    " in " + declaringType.getTypeName() + " is not static");
        try {
            return method.invoke(null).toString();
        } catch (RuntimeException e) {
            if (e instanceof GraphQlClientException)
                throw e;
            throw new GraphQlClientException("can't resolve header method expression '" + methodName + "'" +
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
        GraphQlClientApi annotation = api.getAnnotation(GraphQlClientApi.class);
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

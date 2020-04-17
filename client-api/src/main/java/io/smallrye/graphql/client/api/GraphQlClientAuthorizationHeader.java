package io.smallrye.graphql.client.api;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Base64;
import java.util.function.Supplier;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Provides a BASIC Authorization header with the credentials configured in MP Config.
 * Depending on the constructor used, a different config key prefix is used,
 * with <code>username</code> and <code>password</code> appended.
 * See the constructors for details.
 */
public class GraphQlClientAuthorizationHeader extends GraphQlClientHeader {
    /**
     * Use the {@link GraphQlClientApi#configKey()} annotated on that api interface,
     * or the fully qualified name of the api itself – plus <code>/mp-graphql/</code>
     */
    public GraphQlClientAuthorizationHeader(Class<?> api) {
        this(configKey(api));
    }

    private static String configKey(Class<?> api) {
        GraphQlClientApi annotation = api.getAnnotation(GraphQlClientApi.class);
        if (annotation == null || annotation.configKey().isEmpty())
            return api.getName();
        return annotation.configKey();
    }

    /**
     * Don't use any config prefix, i.e. plain <code>username</code>/<code>password</code>.
     */
    public GraphQlClientAuthorizationHeader() {
        this("");
    }

    /**
     * Use that config prefix plus <code>/mp-graphql/</code>, if the prefix is not empty.
     */
    public GraphQlClientAuthorizationHeader(String configKey) {
        super("Authorization", auth(configKey));
    }

    private static Supplier<Object> auth(String configKey) {
        String prefix = (configKey.isEmpty()) ? "" : configKey + "/mp-graphql/";
        return () -> {
            String username = CONFIG.getValue(prefix + "username", String.class);
            String password = CONFIG.getValue(prefix + "password", String.class);
            String token = username + ":" + password;
            return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(UTF_8));
        };
    }

    private static final Config CONFIG = ConfigProvider.getConfig();
}

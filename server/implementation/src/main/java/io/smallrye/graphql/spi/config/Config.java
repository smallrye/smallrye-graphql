package io.smallrye.graphql.spi.config;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.CompletionException;

import org.jboss.logging.Logger;

import io.smallrye.graphql.execution.datafetcher.DataFetcherException;

/**
 * This will load the config service
 * Example, using microprofile config
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public interface Config {
    static Logger LOG = Logger.getLogger(Config.class);
    Config config = init();

    static Config init() {
        Config c;
        try {
            c = ServiceLoader.load(Config.class).iterator().next();
        } catch (Exception ex) {
            c = new Config() {
                @Override
                public String getName() {
                    return "Default";
                }

            };
        }

        LOG.debug("Using [" + c.getName() + "] config services");
        return c;
    }

    static Config get() {
        return config;
    }

    public String getName();

    default String getDefaultErrorMessage() {
        return SERVER_ERROR_DEFAULT_MESSAGE;
    }

    default boolean isPrintDataFetcherException() {
        return false;
    }

    default Optional<List<String>> getHideErrorMessageList() {
        return Optional.empty();
    }

    default Optional<List<String>> getShowErrorMessageList() {
        return Optional.empty();
    }

    default boolean shouldHide(Throwable throwable) {
        List<String> hideList = getHideErrorMessageList().orElse(null);
        return isListed(throwable, hideList);
    }

    default boolean shouldShow(Throwable throwable) {
        List<String> showList = getShowErrorMessageList().orElse(null);
        return isListed(throwable, showList);
    }

    default boolean isListed(Throwable throwable, List<String> classNames) {
        if (classNames == null || classNames.isEmpty() || throwable == null) {
            return false;
        }

        return isListed(throwable.getClass(), classNames);
    }

    default boolean isListed(Class throwableClass, List<String> classNames) {
        if (classNames == null || classNames.isEmpty() || throwableClass == null
                || throwableClass.getName().equals(Object.class.getName())) {
            return false;
        }

        // Check that specific class
        for (String configuredValue : classNames) {
            if (configuredValue.equals(throwableClass.getName())) {
                return true;
            } else if (configuredValue.endsWith("*")) {
                String values = configuredValue.substring(0, configuredValue.length() - 1);
                if (throwableClass.getName().startsWith(values)) {
                    return true;
                }
                continue;
            }
        }

        // Check transitive
        return isListed(throwableClass.getSuperclass(), classNames);
    }

    default Optional<List<String>> getUnwrapExceptions() {
        return Optional.empty();
    }

    default boolean shouldUnwrapThrowable(Throwable t) {
        if (getUnwrapExceptions().isPresent()) {
            if (getUnwrapExceptions().get().contains(t.getClass().getName()) && t.getCause() != null) {
                return true;
            }
        }
        return DEFAULT_UNWRAP_EXCEPTIONS.contains(t.getClass().getName()) && t.getCause() != null;
    }

    default boolean isAllowGet() {
        return false;
    }

    default boolean isAllowPostWithQueryParameters() {
        return false;
    }

    default boolean isIncludeScalarsInSchema() {
        return false;
    }

    default boolean isIncludeDirectivesInSchema() {
        return false;
    }

    default boolean isIncludeSchemaDefinitionInSchema() {
        return false;
    }

    default boolean isIncludeIntrospectionTypesInSchema() {
        return false;
    }

    default boolean isTracingEnabled() {
        return false;
    }

    default boolean isMetricsEnabled() {
        return false;
    }

    default boolean isEventsEnabled() {
        return false;
    }

    default boolean shouldEmitEvents() {
        return isTracingEnabled() || isMetricsEnabled() || isEventsEnabled();
    }

    default boolean isFederationEnabled() {
        return false;
    }

    default boolean isFederationBatchResolvingEnabled() {
        return false;
    }

    default LogPayloadOption logPayload() {
        return LogPayloadOption.off;
    }

    default Optional<Boolean> isParserCaptureIgnoredChars() {
        return Optional.empty();
    }

    default Optional<Boolean> isParserCaptureLineComments() {
        return Optional.empty();
    }

    default Optional<Boolean> isParserCaptureSourceLocation() {
        return Optional.empty();
    }

    default Optional<Integer> getParserMaxTokens() {
        return Optional.empty();
    }

    default Optional<Integer> getParserMaxWhitespaceTokens() {
        return Optional.empty();
    }

    default boolean hasParserOptions() {
        return isParserCaptureIgnoredChars().isPresent()
                || isParserCaptureLineComments().isPresent()
                || isParserCaptureSourceLocation().isPresent()
                || getParserMaxTokens().isPresent()
                || getParserMaxWhitespaceTokens().isPresent();
    }

    default Optional<Integer> getQueryComplexityInstrumentation() {
        return Optional.empty();
    }

    default Optional<Integer> getQueryDepthInstrumentation() {
        return Optional.empty();
    }

    default boolean isExcludeNullFieldsInResponses() {
        return false;
    }

    default String getFieldVisibility() {
        return FIELD_VISIBILITY_DEFAULT;
    }

    default Optional<List<String>> getErrorExtensionFields() {
        return Optional.empty();
    }

    default <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        return defaultValue;
    }

    public static final String SERVER_ERROR_DEFAULT_MESSAGE = "System error";
    public static final String FIELD_VISIBILITY_DEFAULT = "default";
    public static final String FIELD_VISIBILITY_NO_INTROSPECTION = "no-introspection";

    public static final String ERROR_EXTENSION_EXCEPTION = "exception";
    public static final String ERROR_EXTENSION_CLASSIFICATION = "classification";
    public static final String ERROR_EXTENSION_CODE = "code";
    public static final String ERROR_EXTENSION_DESCRIPTION = "description";
    public static final String ERROR_EXTENSION_VALIDATION_ERROR_TYPE = "validationErrorType";
    public static final String ERROR_EXTENSION_QUERY_PATH = "queryPath";

    public static final List<String> ERROR_EXTENSION_ALL_KNOWN = Arrays.asList(
            ERROR_EXTENSION_EXCEPTION,
            ERROR_EXTENSION_CLASSIFICATION,
            ERROR_EXTENSION_CODE,
            ERROR_EXTENSION_DESCRIPTION,
            ERROR_EXTENSION_VALIDATION_ERROR_TYPE,
            ERROR_EXTENSION_QUERY_PATH);

    public static List<String> DEFAULT_UNWRAP_EXCEPTIONS = Arrays.asList(
            CompletionException.class.getName(),
            DataFetcherException.class.getName(),
            "javax.ejb.EJBException",
            "jakarta.ejb.EJBException");
}

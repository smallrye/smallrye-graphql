package io.smallrye.graphql.execution.error;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.smallrye.graphql.api.ErrorExtensionProvider;

/**
 * All ErrorExtensionProvider holder
 *
 * @author Pipinet (pipinet@vip.qq.com)
 */
public class ErrorExtensionProviders {
    private final List<ErrorExtensionProvider> providers;

    protected ErrorExtensionProviders() {
        this.providers = ServiceLoader.load(ErrorExtensionProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
    }

    protected Stream<ErrorExtensionProvider> get() {
        return providers.stream();
    }
}

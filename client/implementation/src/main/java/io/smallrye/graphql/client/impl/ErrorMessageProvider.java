package io.smallrye.graphql.client.impl;

import java.util.ServiceLoader;

/**
 * Some error messages might differ depending on what appserver/runtime we are running inside.
 * SmallRye provides a default error message provider, but some runtimes may choose to override it.
 */
public interface ErrorMessageProvider {

    RuntimeException urlMissingErrorForNamedClient(String name);

    static ErrorMessageProvider get() {
        ServiceLoader<ErrorMessageProvider> providers = ServiceLoader.load(ErrorMessageProvider.class);
        ErrorMessageProvider chosenProvider = null;
        for (ErrorMessageProvider provider : providers) {
            // pick the a provided non-default one, if it exists
            // otherwise use the default provider
            if (chosenProvider == null || !provider.getClass().equals(DefaultErrorMessageProvider.class)) {
                chosenProvider = provider;
            }
        }
        return chosenProvider;
    }

}

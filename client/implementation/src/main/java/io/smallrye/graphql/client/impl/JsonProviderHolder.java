package io.smallrye.graphql.client.impl;

import jakarta.json.spi.JsonProvider;

// A central place to get the JsonProvider to avoid calling `JsonProvider.provider()` many times
// due to associated performance costs.
public class JsonProviderHolder {

    public static final JsonProvider JSON_PROVIDER = JsonProvider.provider();

}

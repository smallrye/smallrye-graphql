package io.smallrye.graphql.client.impl.discovery;

import io.smallrye.mutiny.Uni;

public class StaticURLSupplier implements ServiceURLSupplier {

    private final String url;

    public StaticURLSupplier(String url) {
        this.url = url;
    }

    @Override
    public Uni<String> get() {
        return Uni.createFrom().item(url);
    }

}

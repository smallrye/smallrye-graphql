package io.smallrye.graphql.client.impl.discovery;

import io.smallrye.mutiny.Uni;

public interface ServiceURLSupplier {

    Uni<String> get();

}

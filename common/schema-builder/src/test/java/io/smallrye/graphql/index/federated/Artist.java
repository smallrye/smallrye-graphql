package io.smallrye.graphql.index.federated;

import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.NonNull;

import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.External;
import io.smallrye.graphql.api.federation.Key;

public @Extends @Key(fields = "id") class Artist {
    @External
    @NonNull
    @Id
    String id;

    public Artist(String id) {
        this.id = id;
    }
}

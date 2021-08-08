package io.smallrye.graphql.index.federated;

import io.smallrye.graphql.federation.api.Extends;
import io.smallrye.graphql.federation.api.External;
import io.smallrye.graphql.federation.api.Key;
import org.eclipse.microprofile.graphql.Id;
import org.eclipse.microprofile.graphql.NonNull;

public @Extends
@Key(fields = "id")
class Artist {
  @External
  @NonNull
  @Id
  String id;

  public Artist(String id) {
    this.id = id;
  }
}

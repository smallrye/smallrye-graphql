package io.smallrye.graphql.schema;

import io.smallrye.graphql.api.federation.External;
import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.Key;

@Key(fields = @FieldSet("id"))

public interface TestInterfaceWitFederation {
    @External
    String getId();
}

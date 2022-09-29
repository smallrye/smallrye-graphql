package io.smallrye.graphql.schema;

import io.smallrye.graphql.api.federation.Key;

@Key(fields = "id")
public class TestTypeWithFederation {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

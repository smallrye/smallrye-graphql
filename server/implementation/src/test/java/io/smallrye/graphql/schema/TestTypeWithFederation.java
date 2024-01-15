package io.smallrye.graphql.schema;

import io.smallrye.graphql.api.federation.Key;

@Key(fields = "id")
@Key(fields = "type id", resolvable = true)
public class TestTypeWithFederation {
    private String type;
    private String id;
    private String value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

package io.smallrye.graphql.test.namespace;

import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.Key;

@Key(fields = @FieldSet("id anotherId"))
public class NamedNamespaceWIthGroupingKeyModel {
    private String id;
    private String anotherId;
    private String value;

    public NamedNamespaceWIthGroupingKeyModel() {
    }

    public NamedNamespaceWIthGroupingKeyModel(String id, String anotherId, String value) {
        this.id = id;
        this.anotherId = anotherId;
        this.value = value;
    }

    public String getAnotherId() {
        return anotherId;
    }

    public void setAnotherId(String anotherId) {
        this.anotherId = anotherId;
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

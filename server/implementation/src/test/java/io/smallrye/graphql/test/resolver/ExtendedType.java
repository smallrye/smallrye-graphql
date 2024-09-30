package io.smallrye.graphql.test.resolver;

import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.External;
import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.api.federation.Requires;

@Extends
@Key(fields = @FieldSet("id"))
public class ExtendedType {
    @External
    private String id;

    @External
    private String name;

    @External
    private String key;

    @Requires(fields = @FieldSet("name key"))
    private String value;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

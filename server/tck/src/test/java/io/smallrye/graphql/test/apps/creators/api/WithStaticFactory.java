package io.smallrye.graphql.test.apps.creators.api;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public class WithStaticFactory {

    private final String field;

    private WithStaticFactory(final String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    @JsonbCreator
    public static WithStaticFactory create(@JsonbProperty("field") String field) {
        return new WithStaticFactory(field);
    }
}

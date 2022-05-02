package io.smallrye.graphql.test.jsonbCreator;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public class WithJsonbCreator {

    private final String field;

    @JsonbCreator
    public WithJsonbCreator(@JsonbProperty("field") final String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}

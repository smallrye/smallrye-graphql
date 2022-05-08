package io.smallrye.graphql.test.apps.creators.api;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

import org.eclipse.microprofile.graphql.NumberFormat;

public class CreatorWithTransformation {

    @NumberFormat
    private final Integer field;

    @JsonbCreator
    public CreatorWithTransformation(@JsonbProperty("field") final Integer field) {
        this.field = field;
    }

    public Integer getField() {
        return field;
    }

}

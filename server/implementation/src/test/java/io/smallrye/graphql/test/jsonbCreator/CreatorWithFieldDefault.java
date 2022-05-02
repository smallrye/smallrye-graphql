package io.smallrye.graphql.test.jsonbCreator;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

import org.eclipse.microprofile.graphql.DefaultValue;

public class CreatorWithFieldDefault {

    @DefaultValue("Some value")
    private final String field;

    @JsonbCreator
    public CreatorWithFieldDefault(@JsonbProperty("field") final String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

}

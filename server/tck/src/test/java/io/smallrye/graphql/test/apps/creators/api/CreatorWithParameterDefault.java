package io.smallrye.graphql.test.apps.creators.api;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

import org.eclipse.microprofile.graphql.DefaultValue;

public class CreatorWithParameterDefault {

    private final String field;

    @JsonbCreator
    public CreatorWithParameterDefault(@JsonbProperty("field") @DefaultValue("Some value") final String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

}

package io.smallrye.graphql.test.jsonbCreator;

import java.time.LocalDate;

import jakarta.json.bind.annotation.JsonbCreator;
import jakarta.json.bind.annotation.JsonbProperty;

public class CreatorWithMultipleParameters {

    private final String string;
    private final Integer integer;
    private final LocalDate localDate;

    @JsonbCreator
    public CreatorWithMultipleParameters(@JsonbProperty("string") final String string,
            @JsonbProperty("integer") final Integer integer,
            @JsonbProperty("localDate") final LocalDate localDate) {
        this.string = string;
        this.integer = integer;
        this.localDate = localDate;
    }

    public String getString() {
        return string;
    }

    public Integer getInteger() {
        return integer;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }
}

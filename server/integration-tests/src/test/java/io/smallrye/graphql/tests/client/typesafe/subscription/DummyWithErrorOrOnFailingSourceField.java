package io.smallrye.graphql.tests.client.typesafe.subscription;

import io.smallrye.graphql.client.typesafe.api.ErrorOr;

public class DummyWithErrorOrOnFailingSourceField {

    private ErrorOr<Integer> failingSourceField;
    private Integer number;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public ErrorOr<Integer> getFailingSourceField() {
        return failingSourceField;
    }

    public void setFailingSourceField(ErrorOr<Integer> failingSourceField) {
        this.failingSourceField = failingSourceField;
    }
}

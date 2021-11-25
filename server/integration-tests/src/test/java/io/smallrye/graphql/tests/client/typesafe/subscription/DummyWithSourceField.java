package io.smallrye.graphql.tests.client.typesafe.subscription;

public class DummyWithSourceField {

    private Integer failingSourceField;
    private Integer number;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getFailingSourceField() {
        return failingSourceField;
    }

    public void setFailingSourceField(Integer failingSourceField) {
        this.failingSourceField = failingSourceField;
    }

    @Override
    public String toString() {
        return "DummyWithSourceField{" +
                "failingSourceField=" + failingSourceField +
                ", number=" + number +
                '}';
    }
}

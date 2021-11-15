package io.smallrye.graphql.tests.client.typesafe.subscription;

public class Dummy {

    private Integer number;

    public Dummy() {
    }

    public Dummy(Integer number) {
        this.number = number;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}

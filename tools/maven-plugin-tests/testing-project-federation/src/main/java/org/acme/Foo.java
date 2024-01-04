package org.acme;

import io.smallrye.graphql.api.federation.Key;

@Key(fields = "id")
public class Foo {

    private Integer number;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
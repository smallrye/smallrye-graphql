package io.smallrye.graphql.tests.client.typesafe.directives.model;

import java.util.Objects;

public class SomeClassServer {
    private String id;
    private int number;

    public SomeClassServer() {
    }

    public SomeClassServer(String id, int number) {
        this.id = id;
        this.number = number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SomeClassServer someClass = (SomeClassServer) o;
        return number == someClass.number && Objects.equals(id, someClass.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number);
    }
}

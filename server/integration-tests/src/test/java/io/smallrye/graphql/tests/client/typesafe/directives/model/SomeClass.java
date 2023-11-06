package io.smallrye.graphql.tests.client.typesafe.directives.model;

import java.util.Objects;

import io.smallrye.graphql.tests.client.typesafe.directives.FieldDirective;

public class SomeClass {
    @FieldDirective(fields = 4)
    private String id;
    private int number;

    public SomeClass() {
    }

    public SomeClass(String id, int number) {
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
        SomeClass someClass = (SomeClass) o;
        return number == someClass.number && Objects.equals(id, someClass.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, number);
    }
}

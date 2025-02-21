package io.smallrye.graphql.tests.client.typesafe.generics.clientmodels;

import java.util.Objects;

public class SimpleAnimal {
    public String name;

    public SimpleAnimal() {

    }

    public SimpleAnimal(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SimpleAnimal that = (SimpleAnimal) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "SimpleAnimal{" +
                "name='" + name + '\'' +
                '}';
    }
}

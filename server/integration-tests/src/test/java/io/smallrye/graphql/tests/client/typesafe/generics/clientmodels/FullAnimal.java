package io.smallrye.graphql.tests.client.typesafe.generics.clientmodels;

import java.util.Objects;

public class FullAnimal {
    public String name;
    public int age;
    public int weight;
    public String description;

    public FullAnimal(String name, int age, int weight, String description) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.description = description;
    }

    public FullAnimal() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FullAnimal that = (FullAnimal) o;
        return age == that.age && weight == that.weight && Objects.equals(name, that.name)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age, weight, description);
    }

    @Override
    public String toString() {
        return "FullAnimal{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", weight=" + weight +
                ", description='" + description + '\'' +
                '}';
    }
}

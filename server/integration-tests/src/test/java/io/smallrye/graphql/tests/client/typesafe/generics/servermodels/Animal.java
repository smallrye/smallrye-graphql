package io.smallrye.graphql.tests.client.typesafe.generics.servermodels;

public class Animal {
    public String name;
    public int age;
    public int weight;
    public String description;

    public Animal(String name, int age, int weight, String description) {
        this.name = name;
        this.age = age;
        this.weight = weight;
        this.description = description;
    }
}

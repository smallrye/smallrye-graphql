package io.smallrye.graphql.tests.client.typesafe.generics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.graphql.tests.client.typesafe.generics.servermodels.Animal;
import io.smallrye.mutiny.Multi;

@GraphQLApi
public class AnimalApi {

    final Map<String, Animal> animals = Map.of("elephant", new Animal("elephant", 34, 5000, "A very big animal"),
            "cat", new Animal("cat", 3, 4, "A very cute animal"));

    @Query
    public List<Animal> allAnimals() {
        return new ArrayList<>(animals.values());
    }

    @Query
    public Animal animalWithName(String name) {
        return animals.get(name);
    }

    @Subscription()
    public Multi<Animal> animalsSubscription() {
        return Multi.createFrom().iterable(animals.values());
    }
}

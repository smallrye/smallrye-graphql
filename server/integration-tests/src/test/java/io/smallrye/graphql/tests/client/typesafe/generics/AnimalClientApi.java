package io.smallrye.graphql.tests.client.typesafe.generics;

import java.util.List;

import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface AnimalClientApi<T> {
    @Query
    List<T> allAnimals();

    @Query("allAnimals")
    Uni<List<T>> allAnimalsUni();

    @Query
    T animalWithName(String name);

    @Query("animalWithName")
    Uni<T> animalWithNameUni(String name);

    @Subscription()
    Multi<T> animalsSubscription();
}

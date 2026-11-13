package examples.typesafeclient;

import java.util.List;

import jakarta.inject.Inject;

public class MyClientUsage {

    @Inject
    SuperHeroesApi superHeroesApi;

    public void execute() {
        List<SuperHero> allHeroes = superHeroesApi.allHeroesIn("Outer Space");
        // ...
    }

}

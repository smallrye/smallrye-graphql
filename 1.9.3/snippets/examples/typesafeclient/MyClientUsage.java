package examples.typesafeclient;

import javax.inject.Inject;
import java.util.List;

public class MyClientUsage {

    @Inject
    SuperHeroesApi superHeroesApi;

    public void execute() {
        List<SuperHero> allHeroes = superHeroesApi.allHeroesIn("Outer Space");
        // ...
    }

}

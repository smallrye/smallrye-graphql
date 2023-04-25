package io.smallrye.graphql.index.generic;

import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class HeroResource implements CharacterResource<Hero> {

    @Override
    @Query("heroes")
    public List<Hero> getAll() {
        return null;
    }

    @Override
    @Mutation("addHero")
    public Hero add(@Name("hero") Hero character) {
        return null;
    }

    @Override
    @Mutation("removeHero")
    public Hero remove(@Name("hero") Hero character) {
        return null;
    }

    @Override
    @Mutation("updateHero")
    public Hero update(@Name("hero") Hero character) {
        return null;
    }

    @Override
    @Mutation("doSomething")
    public Hero doSomething() {
        return null;
    }
}

package examples.typesafeclient;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

import java.util.List;

@GraphQLClientApi
public interface SuperHeroesApi {

    List<SuperHero> allHeroesIn(String location);

}

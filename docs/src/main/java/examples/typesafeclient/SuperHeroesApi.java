package examples.typesafeclient;

import java.util.List;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

@GraphQLClientApi
public interface SuperHeroesApi {

    List<SuperHero> allHeroesIn(String location);

}

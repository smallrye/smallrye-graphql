package examples.dynamicclient;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;

import java.util.List;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import examples.typesafeclient.SuperHero;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import tools.jackson.databind.node.ArrayNode;

public class MyClientUsage {

    @Inject
    @GraphQLClient("superHeroes")
    DynamicGraphQLClient client;

    public void execute() throws ExecutionException, InterruptedException {
        Document document = document( // <1>
                operation(field("allHeroesIn",
                        args(arg("location", "Outer Space")),
                        field("name"),
                        field("superPowers"))));
        Response response = client.executeSync(document); // <2>

        ArrayNode heroesArray = (ArrayNode) response.getData().get("allHeroesIn"); // <3>
        List<SuperHero> heroes = response.getList(SuperHero.class, "allHeroesIn"); // <4>
    }
}

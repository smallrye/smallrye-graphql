package examples.dynamicclient;

import examples.typesafeclient.SuperHero;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;

import javax.inject.Inject;
import javax.json.JsonArray;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.smallrye.graphql.client.core.Argument.arg;
import static io.smallrye.graphql.client.core.Argument.args;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;

public class MyClientUsage {

    @Inject
    DynamicGraphQLClient client;

    public void execute() throws ExecutionException, InterruptedException {
        Document document = document(   // <1>
            operation(field("allHeroesIn",
                args(arg("location", "Outer Space")),
                field("name"),
                field("superPowers"))));
        Response response = client.executeSync(document); // <2>

        JsonArray heroesArray = response.getData().getJsonArray("allHeroesIn");  // <3>
        List<SuperHero> heroes = response.getList(SuperHero.class, "allHeroesIn"); // <4>
    }
}

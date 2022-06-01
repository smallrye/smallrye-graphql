package examples.dynamicclient;

import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MyClientUsageString {

    @Inject
    @GraphQLClient("superheroes")
    DynamicGraphQLClient client;

    public void execute() throws ExecutionException, InterruptedException {
        String queryWithInlineVariables =   // <1>
            "query {" +
            "  allHeroesIn(location: \"Outer Space\") {" +
            "    name" +
            "    superPowers" +
            "  }" +
            "}";
        Response response = client.executeSync(queryWithInlineVariables);

        String queryWithExtractedVariables =   // <2>
            "query($loc: String) {" +
            "  allHeroesIn(location: $loc) {" +
            "    name" +
            "    superPowers" +
            "  }" +
            "}";
        Map<String, Object> variables = new HashMap<>(); // <3>
        variables.put("loc", "Outer Space");
        Response response2 = client.executeSync(queryWithExtractedVariables, variables);
    }
}

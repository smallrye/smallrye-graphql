package tck.graphql.typesafe;

import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

public class RecursionBehavior {

    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    private static class Hero {
        String name;
        List<Team> teams;
    }

    private static class Team {
        String name;
        List<Hero> heroes;
    }

    @GraphQLClientApi
    private interface RecursiveApi {
        Hero member();
    }

    @Test
    void shouldFailToCallApiWithRecursiveFields() {
        RecursiveApi api = fixture.build(RecursiveApi.class);

        IllegalStateException thrown = catchThrowableOfType(api::member, IllegalStateException.class);

        then(thrown).hasMessageContaining("Field recursion found");
    }
}

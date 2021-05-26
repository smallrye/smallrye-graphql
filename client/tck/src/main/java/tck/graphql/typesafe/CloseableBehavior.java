package tck.graphql.typesafe;

import static org.assertj.core.api.BDDAssertions.then;

import java.io.Closeable;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

class CloseableBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface CloseableApi extends Closeable {
        @SuppressWarnings("unused")
        String foo();
    }

    @Test
    void shouldCloseClientNotCall() throws IOException {
        CloseableApi api = fixture.build(CloseableApi.class);

        api.close();

        fixture.verifyClosed();
    }

    @Test
    void shouldNotInvokeToString() {
        CloseableApi api = fixture.build(CloseableApi.class);

        String actual = api.toString();

        then(actual).isNotNull();
    }

    @Test
    void shouldNotInvokeEquals() {
        CloseableApi api = fixture.build(CloseableApi.class);

        @SuppressWarnings("EqualsBetweenInconvertibleTypes")
        boolean equals = api.equals(this);

        then(equals).isFalse();
    }

    @Test
    void shouldNotInvokeHashCode() {
        CloseableApi api = fixture.build(CloseableApi.class);

        int hashCode = api.hashCode();

        then(hashCode).isNotNull();
    }
}

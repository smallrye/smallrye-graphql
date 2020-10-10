package test.unit;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.assertj.core.api.BDDAssertions.then;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQlClientApi;
import io.smallrye.graphql.client.typesafe.api.GraphQlClientException;

public class ErrorBehavior {
    private final GraphQlClientFixture fixture = new GraphQlClientFixture();

    @GraphQlClientApi
    interface StringApi {
        @SuppressWarnings("UnusedReturnValue")
        String greeting();
    }

    @Test
    void shouldFailOnQueryError() {
        fixture.returns(Response.ok("{\"errors\":[{\"message\":\"failed\"}]}"));
        StringApi api = fixture.builder().build(StringApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage("errors from service: [{\"message\":\"failed\"}]:\n" +
                "  {\"query\":\"query greeting { greeting }\",\"variables\":{},\"operationName\":\"greeting\"}");
    }

    @Test
    void shouldFailStringQueryNotFound() {
        fixture.returns(Response.serverError().type(TEXT_PLAIN_TYPE).entity("failed"));
        StringApi api = fixture.builder().build(StringApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage("expected successful status code but got 500 Internal Server Error:\nfailed");
    }

    @Test
    void shouldFailOnMissingQueryResponse() {
        fixture.returnsData("");
        StringApi api = fixture.builder().build(StringApi.class);

        GraphQlClientException thrown = catchThrowableOfType(api::greeting, GraphQlClientException.class);

        then(thrown).hasMessage("no data for 'greeting':\n  {}");
    }

    @Test
    public void shouldIgnoreEmptyError() {
        fixture.returns(Response.ok("{\"errors\":[], \"data\":{\"greeting\":\"dummy-greeting\"}}"));
        StringApi api = fixture.builder().build(StringApi.class);

        String greeting = api.greeting();

        then(fixture.query()).isEqualTo("query greeting { greeting }");
        then(greeting).isEqualTo("dummy-greeting");
    }
}

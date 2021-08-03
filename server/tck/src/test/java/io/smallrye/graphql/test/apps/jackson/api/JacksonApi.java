package io.smallrye.graphql.test.apps.jackson.api;

import java.time.LocalDateTime;
import java.time.Month;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class JacksonApi {

    @Query
    public JacksonObject getJacksonObject() {
        return new JacksonObject("Phillip", "foobar", LocalDateTime.of(1978, Month.JULY, 3, 11, 12));
    }

}

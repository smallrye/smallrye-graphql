package io.smallrye.graphql.tests.nonnull;

import java.math.BigDecimal;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class SomeApi {
    @Query
    public @NonNull Integer echoNumber(@NonNull Integer number) {
        return number;
    }

    @Query
    public @NonNull String echoMessage(@NonNull String message) {
        return message;
    }

    // in this case the '@NonNull' annotations are redundant
    @Mutation
    public @NonNull int add(@NonNull int a, @NonNull int b) {
        return a + b;
    }

    @Query
    public @NonNull BigDecimal echoBigDecimal() {
        return null;
    }
}

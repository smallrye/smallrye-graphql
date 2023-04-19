package io.smallrye.graphql.tests.client.typesafe.voidmutation.client;

import java.util.List;

import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.graphql.client.typesafe.api.TypesafeResponse;
import io.smallrye.graphql.tests.client.typesafe.voidmutation.Rectangle;
import io.smallrye.mutiny.Uni;

@GraphQLClientApi
public interface RectangleClientApi {
    @Mutation
    String resetRectangles();

    @Query
    List<Rectangle> findAllRectangles();

    @Mutation
    Void createRectangle(Rectangle rectangle);

    @Mutation
    Void createRectangleError(Rectangle rectangle);

    @Mutation
    Uni<Void> someUniMutation(Rectangle rectangle);

    @Mutation
    Uni<Void> someUniMutationThrowsError(Rectangle rectangle);

    @Mutation
    TypesafeResponse<Void> typeSafeCreateRectangle(Rectangle rectangle);

    @Mutation
    void primitiveCreateRectangle(Rectangle rectangle);

    @Mutation
    void primitiveCreateRectangleError(Rectangle rectangle);
}

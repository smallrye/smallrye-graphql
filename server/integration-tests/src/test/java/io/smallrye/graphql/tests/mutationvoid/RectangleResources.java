package io.smallrye.graphql.tests.mutationvoid;

import java.util.List;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.mutiny.Uni;

@GraphQLApi
public class RectangleResources {
    @Inject
    private RectangleService rectangleService;

    @Mutation
    public String resetRectangles() {
        rectangleService.reset();
        return "ok";
    }

    @Query
    public List<Rectangle> findAllRectangles() {
        return rectangleService.getRectangles();
    }

    @Mutation
    public Void createRectangle(Rectangle rectangle) {
        rectangleService.createRectangle(rectangle);
        return null;
    }

    @Mutation
    public Void createRectangleError(Rectangle rectangle) {
        throw new RuntimeException("This is for testing purposes");
    }

    @Mutation
    public Uni<Void> someUniMutation(Rectangle rectangle) {
        return Uni.createFrom().voidItem();
    }

    @Mutation
    public Uni<Void> someUniMutationThrowsError(Rectangle rectangle) {
        return Uni.createFrom().voidItem().onItem().failWith(() -> new RuntimeException());
    }

    @Mutation
    public void primitiveCreateRectangle(Rectangle rectangle) {
        rectangleService.createRectangle(rectangle);
    }

    @Mutation
    public void primitiveCreateRectangleError(Rectangle rectangle) {
        throw new RuntimeException("This is for testing purposes");
    }

}

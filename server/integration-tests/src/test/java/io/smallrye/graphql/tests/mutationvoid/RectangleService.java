package io.smallrye.graphql.tests.mutationvoid;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RectangleService {
    private List<Rectangle> rectangles;

    public RectangleService() {
        reset();
    }

    public void reset() {
        rectangles = new ArrayList<>(
                List.of(
                        new Rectangle(3.14, 5.0),
                        new Rectangle(14.2, 2.2),
                        new Rectangle(43.1, 23.6)));
    }

    public List<Rectangle> getRectangles() {
        return rectangles;
    }

    public void createRectangle(Rectangle rectangle) { // void
        rectangles.add(rectangle);
    }

}

package io.smallrye.graphql.test.apps.interfaces.api;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class FoodResource {

    @Query
    public Eatable getApple() {
        return new Apple("Granny Smith", "green");
    }

    public static class Apple implements Eatable {

        private String name;
        private String color;

        public Apple() {
        }

        public Apple(String name, String color) {
            this.name = name;
            this.color = color;
        }

        @Override
        public String getColor() {
            return color;
        }

        @Override
        public void setColor(String color) {
            this.color = color;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void setName(String name) {
            this.name = name;
        }

    }

}
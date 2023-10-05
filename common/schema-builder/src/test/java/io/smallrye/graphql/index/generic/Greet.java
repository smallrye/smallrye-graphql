package io.smallrye.graphql.index.generic;

public class Greet implements ResponseAttribute<String> {
    String value;

    public Greet(String name) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return "hello".concat(value);
    }
}

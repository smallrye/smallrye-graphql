package io.smallrye.graphql.index.app;

public class Mapped {
    private String value;

    public Mapped() {
    }

    public Mapped(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
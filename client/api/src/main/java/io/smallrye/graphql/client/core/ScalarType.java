package io.smallrye.graphql.client.core;

public enum ScalarType {
    GQL_INT("Int"),
    GQL_FLOAT("Float"),
    GQL_STRING("String"),
    GQL_BOOL("Boolean"),
    GQL_ID("ID");

    private String type;

    ScalarType(String type) {
        this.type = type;
    }

    public String toString() {
        return type;
    }
}

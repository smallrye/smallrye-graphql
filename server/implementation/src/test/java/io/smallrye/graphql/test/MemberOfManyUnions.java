package io.smallrye.graphql.test;

public class MemberOfManyUnions implements TestUnion, UnionOfInterfaces {

    private String message;

    public MemberOfManyUnions() {
    }

    public MemberOfManyUnions(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

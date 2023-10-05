package io.smallrye.graphql.index.generic;

public class ResponseComposite {
    Greet greet;

    public ResponseComposite(Greet greet) {
        this.greet = greet;
    }

    public Greet getGreet() {
        return this.greet;
    }

    public void setGreet(Greet greet) {
        this.greet = greet;
    }
}

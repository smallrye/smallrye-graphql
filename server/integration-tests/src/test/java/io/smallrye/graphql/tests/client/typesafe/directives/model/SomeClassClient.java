package io.smallrye.graphql.tests.client.typesafe.directives.model;

import org.eclipse.microprofile.graphql.Name;

import io.smallrye.graphql.tests.client.typesafe.directives.FieldDirective;

@Name("SomeClassServerInput")
public class SomeClassClient {
    @FieldDirective(fields = 4)
    String id;
    int number;

    public SomeClassClient(String id, int number) {
        this.id = id;
        this.number = number;
    }

    public SomeClassClient() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}

package io.smallrye.graphql.index.duplicates.source;

public class SomeClass {
    private String name;
    private String password;

    public SomeClass() {
    }

    public SomeClass(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

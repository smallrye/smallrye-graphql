package io.smallrye.graphql.index.duplicates.targetinput;

public class SomeInput {
    private String name;
    private String password;

    public SomeInput() {
    }

    public SomeInput(String name, String password) {
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

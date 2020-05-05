package io.smallrye.graphql.index.app;

public class Person {
    String firstName;
    String lastName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Person && firstName.equals(((Person) o).firstName)
                && lastName.equals(((Person) o).lastName);
    }
}
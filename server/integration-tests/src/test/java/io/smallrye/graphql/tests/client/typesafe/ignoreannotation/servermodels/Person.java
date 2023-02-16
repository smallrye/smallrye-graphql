package io.smallrye.graphql.tests.client.typesafe.ignoreannotation.servermodels;

import java.util.List;

public class Person {
    long id;
    String firstname;
    String surname;
    List<Person> children;

    public Person() {
    }

    public Person(long id,
            String firstname,
            String surname,
            List<Person> children) {
        setId(id);
        setFirstname(firstname);
        setSurname(surname);
        setChildren(children);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public List<Person> getChildren() {
        return children;
    }

    public void setChildren(List<Person> children) {
        this.children = children;
    }
}

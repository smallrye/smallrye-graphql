package io.smallrye.graphql.tests.client.typesafe.ignoreannotation.clientmodels;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.json.bind.annotation.JsonbTransient;

import org.eclipse.microprofile.graphql.Ignore;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Person {
    BigDecimal id;
    @Ignore
    String firstname;
    @JsonbTransient
    String surname;
    @JsonIgnore
    List<Person> children;
    @Ignore
    Set<Person> parents; // Field does not exist on the server side.

    public BigDecimal getId() {
        return id;
    }

    public void setId(BigDecimal id) {
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

    public Set<Person> getParents() {
        return parents;
    }

    public void setParents(Set<Person> parents) {
        this.parents = parents;
    }

    public Person() {
    }

    public Person(BigDecimal id,
            String firstname,
            String surname,
            List<Person> children,
            Set<Person> parents) {
        this.id = id;
        this.firstname = firstname;
        this.surname = surname;
        this.children = children;
        this.parents = parents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) && Objects.equals(firstname, person.firstname)
                && Objects.equals(surname, person.surname) && Objects.equals(children, person.children)
                && Objects.equals(parents, person.parents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstname, surname, children, parents);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", surname='" + surname + '\'' +
                ", children=" + children +
                ", parents=" + parents +
                '}';
    }
}

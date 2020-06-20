package io.smallrye.graphql.index.app;

import java.util.Date;
import java.util.Set;

import io.smallrye.graphql.api.Scalar;
import io.smallrye.graphql.api.ToScalar;

public class Movie {

    @ToScalar(Scalar.Int.class)
    Long idLongThatShouldChangeToInt;
    String title;
    Date releaseDate;
    Person director;
    Set<Person> topBilledCast;

    public Long getIdLongThatShouldChangeToInt() {
        return idLongThatShouldChangeToInt;
    }

    public void setIdLongThatShouldChangeToInt(Long idLongThatShouldChangeToInt) {
        this.idLongThatShouldChangeToInt = idLongThatShouldChangeToInt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Person getDirector() {
        return director;
    }

    public void setDirector(Person director) {
        this.director = director;
    }

    public Set<Person> getTopBilledCast() {
        return topBilledCast;
    }

    public void setTopBilledCast(Set<Person> topBilledCast) {
        this.topBilledCast = topBilledCast;
    }
}

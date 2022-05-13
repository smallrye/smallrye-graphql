package io.smallrye.graphql.test.apps.generics.inheritance.api;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import io.smallrye.mutiny.Uni;

@ApplicationScoped
public class FilmService {

    private List<Film> films = new ArrayList<>();

    public FilmService() {
        Film aNewHope = new Film(
                new TestID("1"), "A New Hope", 4, "George Lucas", LocalDate.of(1977, Month.JANUARY, 25));

        Film theEmpireStrikesBack = new Film(
                new TestID("2"), "The Empire Strikes Back", 5, "Irvin Kershner", LocalDate.of(1980,
                        Month.MARCH, 17));

        Film returnOfTheJedi = new Film(
                new TestID("3"), "Return of the Jedi", 6, "Richard Marquand", LocalDate.of(1983, Month.MAY,
                        25));

        films.add(aNewHope);
        films.add(theEmpireStrikesBack);
        films.add(returnOfTheJedi);
    }

    public Uni<List<Film>> getFilms() {
        System.out.println("Films get method called");

        for (Film f : films) {
            System.out.println(f);
        }

        return Uni.createFrom().item(this.films);
    }

    public Uni<Boolean> addFilm(Film f) {
        return Uni.createFrom().item(this.films.add(f));
    }

    public Uni<Boolean> deleteFilm(int id) {
        try {
            this.films.remove(id);
            return Uni.createFrom().item(true);
        }

        catch (IndexOutOfBoundsException | UnsupportedOperationException e) {
            return Uni.createFrom().item(false);
        }
    }
}

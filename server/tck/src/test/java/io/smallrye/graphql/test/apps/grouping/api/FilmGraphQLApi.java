package io.smallrye.graphql.test.apps.grouping.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

/**
 * Films API
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@GraphQLApi
@Name("films")
@Description("Allow all film releated APIs")
public class FilmGraphQLApi {

    @Query
    public List<Film> getAllFilms() {
        return new ArrayList<>(FILMS.values());
    }

    @Query
    public Film getFilm(String name) {
        return FILMS.get(name);
    }

    public URL searchLink(@Source Film film) {
        String url = "https://www.google.com/search?q=Film+" + film.title.replaceAll(" ", "+");
        try {
            return new URL(url);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Mutation
    public Film addFilm(Film film) {
        FILMS.put(film.title, film);
        return film;
    }

    private static Map<String, Film> FILMS = new HashMap<>();
    static {
        Film film1 = new Film("Fight Club", LocalDate.of(2000, Month.JANUARY, 28), "Brad Pitt", "Edward Norton",
                "Helena Bonham Carter");
        FILMS.put(film1.title, film1);

        Film film2 = new Film("The Game", LocalDate.of(1998, Month.JANUARY, 9), "Michael Douglas", "Deborah Kara Unger",
                "Sean Penn");
        FILMS.put(film2.title, film2);
    }
}

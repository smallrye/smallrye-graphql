package io.smallrye.graphql.index.app;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class MovieTriviaController {

    Set<Movie> movies = new HashSet<>();

    @Mutation
    public Movie newMovie(Movie movie) {
        movies.add(movie);
        return movie;
    }

    @Query
    public Set<Movie> movies() {
        return movies;
    }

    @Query
    public Set<Movie> moviesDirectedBy(@Name("director") Person director) {
        return movies.stream().filter(m -> m.getDirector().equals(director)).collect(Collectors.toSet());
    }

    @Query
    public Set<Movie> moviesReleasedAfter(@Name("date") Date d) {
        return movies.stream().filter(m -> m.getReleaseDate().after(d)).collect(Collectors.toSet());
    }
}
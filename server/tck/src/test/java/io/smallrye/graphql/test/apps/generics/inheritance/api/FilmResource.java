package io.smallrye.graphql.test.apps.generics.inheritance.api;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.mutiny.Uni;

@GraphQLApi
public class FilmResource {

    @Inject
    FilmService service;

    @Query("allFilms")
    public Uni<List<Film>> getFilms() {
        return this.service.getFilms();
    }

    @Mutation
    public Uni<Boolean> addFilm(final Film f) {
        return this.service.addFilm(f);
    }

    @Mutation
    public Uni<Boolean> deleteFilm(int id) {
        return this.service.deleteFilm(id);
    }
}

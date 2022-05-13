package io.smallrye.graphql.test.apps.generics.inheritance.api;

import java.time.LocalDate;

public class Film extends AbstractHasID<TestID> {

    private String title;
    private Integer episode;
    private String director;
    private LocalDate releaseDate;

    public Film() {

    }

    public Film(
            final TestID id,
            final String title,
            final int episode,
            final String director,
            final LocalDate releaseDate) {
        this.id = id;
        this.title = title;
        this.episode = episode;
        this.director = director;
        this.releaseDate = releaseDate;
    }

    public String getTitle() {
        return this.title;
    }

    public Integer getEpisode() {
        return this.episode;
    }

    public String getDirector() {
        return this.director;
    }

    public LocalDate getReleaseDate() {
        return this.releaseDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setEpisode(Integer episode) {
        this.episode = episode;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Id: ").append(this.id.toString()).append("\n");
        sb.append("Title: ").append(this.title).append("\n");
        sb.append("Episode: ").append(this.episode).append("\n");
        sb.append("Director: ").append(this.director).append("\n");
        sb.append("ReleaseDate: ").append(this.releaseDate).append("\n");

        return sb.toString();
    }
}
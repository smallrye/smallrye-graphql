package io.smallrye.graphql.test.apps.grouping.api;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Film POJO
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Film {

    public String title;
    public LocalDate published;
    public List<String> leadActors;

    public Film() {
    }

    public Film(String title, LocalDate published, String... actors) {
        this.title = title;
        this.published = published;
        this.leadActors = Arrays.asList(actors);
    }

}

package io.smallrye.graphql.test.apps.mutiny.api;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class MutinyBook {
    public String isbn;
    public String title;
    public LocalDate published;
    public List<String> authors;

    public MutinyBook() {
    }

    public MutinyBook(String isbn, String title, LocalDate published, String... authors) {
        this.isbn = isbn;
        this.title = title;
        this.published = published;
        this.authors = Arrays.asList(authors);
    }

}

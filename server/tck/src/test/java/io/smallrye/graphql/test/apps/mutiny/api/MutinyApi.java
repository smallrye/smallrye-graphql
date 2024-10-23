package io.smallrye.graphql.test.apps.mutiny.api;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

@GraphQLApi
public class MutinyApi {

    @Query
    public Uni<MutinyBook> getMutinyBook(String name) {
        return Uni.createFrom().item(() -> BOOKS.get(name));
    }

    public Uni<String> getBuyLink(@Source MutinyBook book) {
        String title = book.title.replaceAll(" ", "+");
        return Uni.createFrom().item(() -> String.format(AMAZON_SEARCH_FORMAT, title));
    }

    public Uni<List<List<MutinyAuthor>>> getSourceAuthors(@Source List<MutinyBook> books) {
        List<List<MutinyAuthor>> authorsOfAllBooks = new ArrayList<>();
        for (MutinyBook book : books) {
            List<MutinyAuthor> authors = new ArrayList<>();
            for (String name : book.fieldAuthors) {
                authors.add(AUTHORS.get(name));
            }
            authorsOfAllBooks.add(authors);
        }
        return Uni.createFrom().item(() -> authorsOfAllBooks);
    }

    private static final String AMAZON_SEARCH_FORMAT = "https://www.amazon.com/s?k=%s&i=stripbooks-intl-ship";
    private static Map<String, MutinyBook> BOOKS = new HashMap<>();
    private static Map<String, MutinyAuthor> AUTHORS = new HashMap<>();
    static {

        // We need this until Wildfly supports Mutiny
        ThreadContext threadContext = ThreadContext.builder().build();
        Infrastructure.setDefaultExecutor(threadContext.currentContextExecutor());

        MutinyBook book1 = new MutinyBook("0-571-05686-5", "Lord of the Flies", LocalDate.of(1954, Month.SEPTEMBER, 17),
                "William Golding");
        BOOKS.put(book1.title, book1);
        AUTHORS.put("William Golding", new MutinyAuthor("William Golding", "William Gerald Golding",
                LocalDate.of(1911, Month.SEPTEMBER, 19), "Newquay, Cornwall, England"));

        MutinyBook book2 = new MutinyBook("0-582-53008-3", "Animal Farm", LocalDate.of(1945, Month.AUGUST, 17),
                "George Orwell");
        BOOKS.put(book2.title, book2);
        AUTHORS.put("George Orwell", new MutinyAuthor("George Orwell", "Eric Arthur Blair", LocalDate.of(1903, Month.JUNE, 25),
                "Motihari, Bengal Presidency, British India"));
    }
}

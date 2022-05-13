package io.smallrye.graphql.test.mutiny;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.mutiny.Uni;

@GraphQLApi
@Description("Allow all mutini book releated APIs")
public class MutinyBookGraphQLApi {

    @Query("book")
    public Uni<Book> getUniBook(String name) {
        return Uni.createFrom().item(BOOKS.get(name));
    }

    @Query("failedBook")
    public Uni<Book> failedBook(@SuppressWarnings("unused") String name) {
        return Uni.createFrom().failure(new CustomException());
    }

    private static final Map<String, Book> BOOKS = new HashMap<>();

    static {
        Book book1 = new Book("0-571-05686-5", "Lord of the Flies", LocalDate.of(1954, Month.SEPTEMBER, 17), "William Golding");
        BOOKS.put(book1.title, book1);

        Book book2 = new Book("0-582-53008-3", "Animal Farm", LocalDate.of(1945, Month.AUGUST, 17), "George Orwell");
        BOOKS.put(book2.title, book2);
    }
}

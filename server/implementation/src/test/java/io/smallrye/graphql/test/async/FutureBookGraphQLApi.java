package io.smallrye.graphql.test.async;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

/**
 * Book API
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@GraphQLApi
@Description("Allow all future book releated APIs")
public class FutureBookGraphQLApi {

    @Query("book")
    public CompletableFuture<Book> getFutureBook(String name) {
        return CompletableFuture.supplyAsync(() -> BOOKS.get(name));
    }

    private static Map<String, Book> BOOKS = new HashMap<>();
    static {
        Book book1 = new Book("0-571-05686-5", "Lord of the Flies", LocalDate.of(1954, Month.SEPTEMBER, 17), "William Golding");
        BOOKS.put(book1.title, book1);

        Book book2 = new Book("0-582-53008-3", "Animal Farm", LocalDate.of(1945, Month.AUGUST, 17), "George Orwell");
        BOOKS.put(book2.title, book2);
    }
}

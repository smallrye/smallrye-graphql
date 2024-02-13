package io.smallrye.graphql.schema.link;

import static io.smallrye.graphql.api.federation.link.Link.FEDERATION_SPEC_LATEST_URL;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.federation.link.Import;
import io.smallrye.graphql.api.federation.link.Link;
import io.smallrye.graphql.api.federation.link.Purpose;

@GraphQLApi
@Link(url = FEDERATION_SPEC_LATEST_URL, _import = {
        @Import(name = "@authenticated"),
        @Import(name = "@external"),
        @Import(name = "@inaccessible"),
        @Import(name = "@policy", as = "@myPolicy"),
        @Import(name = "@tag", as = "@newTag"),
        @Import(name = "@requires"),
        @Import(name = "FieldSet", as = "Field")
}, as = "smallrye", _for = Purpose.EXECUTION)
@Link(url = "https://smallrye.io/custom/v1.0", _import = { @Import(name = "@custom", as = "@smallryeCustom") })
public class Link8Api {

    @Query
    public List<Book> getAllBooks() {
        return new ArrayList<>(BOOKS.values());
    }

    private static Map<String, Book> BOOKS = new HashMap<>();

    static {
        Book book1 = new Book("0-571-05686-5", "Lord of the Flies", LocalDate.of(1954, Month.SEPTEMBER, 17),
                "William Golding");
        BOOKS.put(book1.title, book1);

        Book book2 = new Book("0-582-53008-3", "Animal Farm", LocalDate.of(1945, Month.AUGUST, 17), "George Orwell");
        BOOKS.put(book2.title, book2);
    }
}

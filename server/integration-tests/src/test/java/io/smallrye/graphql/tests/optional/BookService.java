package io.smallrye.graphql.tests.optional;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BookService {
    private List<Book> books;

    public BookService() {
        reset();
    }

    public void reset() {
        books = new ArrayList<>(List.of(
                new Book("BOTR", 1959, OptionalInt.of(1400), OptionalLong.of(1_133_424_341), OptionalDouble.of(4.5)),
                new Book("LOTR", 1954, OptionalInt.of(400), OptionalLong.of(2_053_224_341), OptionalDouble.of(2.3)),
                new Book("HOBIT", 1953, OptionalInt.of(300), OptionalLong.of(132_568_448), OptionalDouble.of(5.0))));
    }

    public List<Book> getBooks() {
        return books;
    }

    public Book getBookByPageCount(OptionalInt pageCount) {
        return books.stream().filter(book -> book
                .getPageCount()
                .equals(pageCount))
                .findFirst()
                .orElseGet(null);
    }

    public Book getBookBySales(OptionalLong sales) {
        return books.stream().filter(book -> book
                .getSales()
                .equals(sales))
                .findFirst()
                .orElseGet(null);
    }

    public Book getBookByRating(OptionalDouble rating) {
        return books.stream().filter(book -> book
                .getRating()
                .equals(rating))
                .findFirst()
                .orElseGet(null);
    }

    public void createBook(Book book) {
        books.add(book);
    }

    public Book deleteBookByPageCount(OptionalInt pageCount) {
        Book deletedBook = books.stream().filter(book -> book.getPageCount().equals(pageCount)).findFirst().orElseGet(null);
        books.remove(deletedBook);
        return deletedBook;
    }
}

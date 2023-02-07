package io.smallrye.graphql.tests.optional;

import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import jakarta.inject.Inject;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class BookResources {
    @Inject
    private BookService bookService;

    @Query
    public List<Book> getBooks() {
        return bookService.getBooks();
    }

    @Query
    public Book getBookByPageCount(OptionalInt pageCount) {
        return bookService.getBookByPageCount(pageCount);
    }

    @Query
    public Book getBookBySales(OptionalLong sales) {
        return bookService.getBookBySales(sales);
    }

    @Query
    public Book getBookByRating(OptionalDouble rating) {
        return bookService.getBookByRating(rating);
    }

    @Mutation
    public String resetBooks() {
        bookService.reset();
        return "ok";
    }

    @Mutation
    public Book createBook(Book book) {
        bookService.createBook(book);
        return book;
    }

    @Mutation
    public Book deleteBookByPageCount(OptionalInt pageCount) {
        return bookService.deleteBookByPageCount(pageCount);
    }

}

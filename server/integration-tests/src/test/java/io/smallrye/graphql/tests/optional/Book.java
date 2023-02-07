package io.smallrye.graphql.tests.optional;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

public class Book {
    private String name;
    private long yearOfRelease;
    private OptionalInt pageCount;
    private OptionalLong sales;
    private OptionalDouble rating;

    public Book(String name,
            long yearOfRelease,
            OptionalInt pageCount,
            OptionalLong sales,
            OptionalDouble rating) {
        this.name = name;
        this.yearOfRelease = yearOfRelease;
        this.pageCount = pageCount;
        this.sales = sales;
        this.rating = rating;
    }

    public Book() {
    }

    public String getName() {
        return name;
    }

    public long getYearOfRelease() {
        return yearOfRelease;
    }

    public OptionalInt getPageCount() {
        return pageCount;
    }

    public OptionalLong getSales() {
        return sales;
    }

    public OptionalDouble getRating() {
        return rating;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setYearOfRelease(long yearOfRelease) {
        this.yearOfRelease = yearOfRelease;
    }

    public void setPageCount(OptionalInt pageCount) {
        this.pageCount = pageCount;
    }

    public void setSales(OptionalLong sales) {
        this.sales = sales;
    }

    public void setRating(OptionalDouble rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                ", yearOfRelease=" + yearOfRelease +
                ", pageCount=" + pageCount +
                ", sales=" + sales +
                ", rating=" + rating +
                '}';
    }
}
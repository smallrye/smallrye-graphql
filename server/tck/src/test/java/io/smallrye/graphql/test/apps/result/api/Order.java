package io.smallrye.graphql.test.apps.result.api;

import java.time.LocalDate;

import org.eclipse.microprofile.graphql.Id;

public class Order {
    private @Id String id;
    private LocalDate orderDate;

    public Order() {
        this.id = null;
        this.orderDate = null;
    }

    public Order(String id, LocalDate orderDate) {
        this.id = id;
        this.orderDate = orderDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }
}

package io.smallrye.graphql.test;

import java.util.Random;

/**
 * Some object to test lists of POJO
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TestListObject {

    private int id;
    private double amount;

    public TestListObject() {
        id = new Random().nextInt();
        amount = Math.random();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

}

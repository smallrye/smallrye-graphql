package io.smallrye.graphql.index.app;

public class Phone {
    private String number;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return number;
    }

    public static Phone fromString(String number) {
        Phone phone = new Phone();
        phone.setNumber(number);
        return phone;
    }
}
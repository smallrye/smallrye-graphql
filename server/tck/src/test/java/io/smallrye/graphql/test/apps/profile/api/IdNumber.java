package io.smallrye.graphql.test.apps.profile.api;

public class IdNumber {

    private String number;

    public static IdNumber fromString(String number) {
        IdNumber idNumber = new IdNumber();
        idNumber.number = number;
        return idNumber;
    }

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
}

package io.smallrye.graphql.test.apps.adapt.with.api;

/**
 * An email complex object
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class EmailAddress {
    private String value;

    public EmailAddress() {
    }

    public EmailAddress(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "EmailAddress{" + "value=" + value + '}';
    }
}
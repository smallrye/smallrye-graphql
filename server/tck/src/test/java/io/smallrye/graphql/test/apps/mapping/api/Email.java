package io.smallrye.graphql.test.apps.mapping.api;

/**
 * An email complex object
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Email {
    private String value;

    public Email(String value) {
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
        return value;
    }
}

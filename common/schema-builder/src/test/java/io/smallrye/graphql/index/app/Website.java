package io.smallrye.graphql.index.app;

import java.net.MalformedURLException;
import java.net.URL;

public class Website {
    private URL value;

    public URL getValue() {
        return value;
    }

    public void setValue(String value) {
        try {
            this.value = new URL(value);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String toString() {
        if (value != null) {
            return value.toString();
        }
        return null;
    }
}

package io.smallrye.graphql.test.apps.profile.api;

import io.smallrye.graphql.api.Scalar;
import io.smallrye.graphql.api.ToScalar;

@ToScalar(Scalar.String.class)
public class TwitterHandle {
    private String value;

    public TwitterHandle() {
    }

    public TwitterHandle(String value) {
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

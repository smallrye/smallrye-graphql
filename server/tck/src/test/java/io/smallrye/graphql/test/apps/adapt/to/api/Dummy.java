package io.smallrye.graphql.test.apps.adapt.to.api;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

public class Dummy {

    public String name;
    @AdaptToScalar(Scalar.String.class)
    public DummyId id;

}

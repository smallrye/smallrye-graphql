package io.smallrye.graphql.test.apps.adapt.with.api;

import io.smallrye.graphql.api.AdaptWith;

public class Dommie {

    public String name;
    @AdaptWith(DommieIdAdapter.class)
    public DommieId id;

}

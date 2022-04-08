package io.smallrye.graphql.test.apps.adapt.with.api;

import io.smallrye.graphql.api.Adapter;

/**
 * Using an adapter
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class DommieIdAdapter implements Adapter<DommieId, Id> {

    @Override
    public Id to(DommieId o) throws Exception {
        Id id = new Id();
        id.value = o.toHexString();
        return id;
    }

    @Override
    public DommieId from(Id a) throws Exception {
        return new DommieId(a.value);
    }

}
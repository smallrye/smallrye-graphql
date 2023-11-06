package io.smallrye.graphql.tests.objectid;

import org.bson.types.ObjectId;

import io.smallrye.graphql.api.Adapter;

public class ObjectIdAdapter implements Adapter<ObjectId, String> {
    @Override
    public ObjectId from(String o) {
        return new ObjectId(o);
    }

    @Override
    public String to(ObjectId a) throws Exception {
        return a.toHexString();
    }
}

package io.smallrye.graphql.test.apps.adapt.to.api;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

@GraphQLApi
public class AdapToResource {

    @Query
    public AdaptToData getAdaptToData() {

        AdaptToData d = new AdaptToData();
        d.id = 1L;
        d.withConstructor = new WithConstructor("bar");

        WithSetValue withSetValue = new WithSetValue();
        withSetValue.setFoo("bar");
        d.withSetValue = withSetValue;

        WithStaticFrom withStaticFrom = new WithStaticFrom();
        withStaticFrom.setFoo("bar");
        d.withStaticFrom = withStaticFrom;

        WithGetInstance withGetInstance = new WithGetInstance();
        withGetInstance.setFoo("bar");
        d.withGetInstance = withGetInstance;

        return d;
    }

    @Query
    public List<AdaptToData> getAdaptToDatas() {
        List<AdaptToData> adapterDatas = new ArrayList<>();
        adapterDatas.add(getAdaptToData());
        return adapterDatas;
    }

    @Mutation
    public AdaptToData updateAdaptToData(AdaptToData adaptToData) {
        return adaptToData;
    }

    public static class AdaptToData {
        public Long id;

        @AdaptToScalar(Scalar.String.class)
        public WithConstructor withConstructor;

        @AdaptToScalar(Scalar.String.class)
        public WithSetValue withSetValue;

        @AdaptToScalar(Scalar.String.class)
        public WithStaticFrom withStaticFrom;

        @AdaptToScalar(Scalar.String.class)
        public WithGetInstance withGetInstance;
    }

}

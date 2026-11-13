package io.smallrye.graphql.test.overflowadapter;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.AdaptWith;
import io.smallrye.graphql.api.Adapter;

@GraphQLApi
public class OverflowAdapterApi {

    @Query
    public Long overflowAdaptedLong(@NonNull @AdaptWith(LongAdapter.class) LongValue input) {
        return input.value;
    }

    public static class LongValue {
        private final Long value;

        public LongValue(Long value) {
            this.value = value;
        }
    }

    public static class LongAdapter implements Adapter<LongValue, Long> {
        @Override
        public LongValue from(Long value) {
            return new LongValue(value);
        }

        @Override
        public Long to(LongValue value) {
            return value.value;
        }
    }
}

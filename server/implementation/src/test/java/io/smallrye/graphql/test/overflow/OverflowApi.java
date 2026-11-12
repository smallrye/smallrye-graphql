package io.smallrye.graphql.test.overflow;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class OverflowApi {

    @Query
    public String overflowPojo(@NonNull NumberPojo input) {
        return input.primitiveInt + ":" + input.boxedInt + ":" + input.primitiveLong + ":" + input.boxedLong;
    }

    @Query
    public String overflowRecord(@NonNull NumberRecord input) {
        return input.primitiveInt() + ":" + input.boxedInt() + ":" + input.primitiveLong() + ":" + input.boxedLong();
    }

    public static class NumberPojo {
        public int primitiveInt;
        @NonNull
        public Integer boxedInt;
        public long primitiveLong;
        @NonNull
        public Long boxedLong;
    }

    public record NumberRecord(int primitiveInt, @NonNull Integer boxedInt,
            long primitiveLong, @NonNull Long boxedLong) {
    }
}

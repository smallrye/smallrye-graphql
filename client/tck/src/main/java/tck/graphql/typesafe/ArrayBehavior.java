package tck.graphql.typesafe;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.jupiter.api.Test;

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

public class ArrayBehavior {
    private final TypesafeGraphQLClientFixture fixture = TypesafeGraphQLClientFixture.load();

    @GraphQLClientApi
    interface BoolApi {
        boolean[] bool(boolean[] in);
    }

    @Test
    void shouldCallBoolArrayQuery() {
        fixture.returnsData("'bool':[true,false]");
        BoolApi api = fixture.build(BoolApi.class);

        boolean[] bool = api.bool(new boolean[] { true, false });

        then(fixture.query()).isEqualTo("query bool($in: [Boolean!]) { bool(in: $in) }");
        then(bool[0]).isTrue();
        then(bool[1]).isFalse();
    }

    @Test
    void shouldCallEmptyBoolArrayQuery() {
        fixture.returnsData("'bool':[]");
        BoolApi api = fixture.build(BoolApi.class);

        boolean[] bool = api.bool(new boolean[0]);

        then(fixture.query()).isEqualTo("query bool($in: [Boolean!]) { bool(in: $in) }");
        then(bool).isEmpty();
    }

    @GraphQLClientApi
    interface BooleanApi {
        Boolean[] bool(Boolean[] in);
    }

    @Test
    void shouldCallBooleanArrayQuery() {
        fixture.returnsData("'bool':[true,false]");
        BooleanApi api = fixture.build(BooleanApi.class);

        Boolean[] bool = api.bool(new Boolean[] { true, false });

        then(fixture.query()).isEqualTo("query bool($in: [Boolean]) { bool(in: $in) }");
        then(bool[0]).isTrue();
        then(bool[1]).isFalse();
    }

    // -------------------------

    @GraphQLClientApi
    interface CharApi {
        char[] chars(char[] in);
    }

    @Test
    void shouldCallCharArrayQuery() {
        fixture.returnsData("'chars':['a','b','c']");
        CharApi api = fixture.build(CharApi.class);

        char[] chars = api.chars(new char[] { 'a', 'b', 'c' });

        then(fixture.query()).isEqualTo("query chars($in: [String!]) { chars(in: $in) }");
        then(chars).containsExactly('a', 'b', 'c');
    }

    @GraphQLClientApi
    interface CharacterApi {
        Character[] chars(Character[] in);
    }

    @Test
    void shouldCallCharacterArrayQuery() {
        fixture.returnsData("'chars':['a','b','c']");
        CharacterApi api = fixture.build(CharacterApi.class);

        Character[] chars = api.chars(new Character[] { 'a', 'b', 'c' });

        then(fixture.query()).isEqualTo("query chars($in: [String]) { chars(in: $in) }");
        then(chars).containsExactly('a', 'b', 'c');
    }

    // -------------------------

    @GraphQLClientApi
    interface PrimitiveShortApi {
        short[] shorts(short[] in);
    }

    @Test
    void shouldCallShortArrayQuery() {
        fixture.returnsData("'shorts':[1,2,3]");
        PrimitiveShortApi api = fixture.build(PrimitiveShortApi.class);

        short[] shorts = api.shorts(new short[] { 1, 2, 3 });

        then(fixture.query()).isEqualTo("query shorts($in: [Int!]) { shorts(in: $in) }");
        then(shorts).containsExactly(1, 2, 3);
    }

    @GraphQLClientApi
    interface ShortApi {
        Short[] shorts(Short[] in);
    }

    @Test
    void shouldCallPrimitiveShortArrayQuery() {
        fixture.returnsData("'shorts':[1,2,3]");
        ShortApi api = fixture.build(ShortApi.class);

        Short[] shorts = api.shorts(new Short[] { 1, 2, 3 });

        then(fixture.query()).isEqualTo("query shorts($in: [Int]) { shorts(in: $in) }");
        then(shorts).containsExactly((short) 1, (short) 2, (short) 3);
    }

    // -------------------------

    @GraphQLClientApi
    interface IntApi {
        int[] integer(int[] in);
    }

    @Test
    void shouldCallIntArrayQuery() {
        fixture.returnsData("'integer':[1,2,3]");
        IntApi api = fixture.build(IntApi.class);

        int[] ints = api.integer(new int[] { 1, 2, 3 });

        then(fixture.query()).isEqualTo("query integer($in: [Int!]) { integer(in: $in) }");
        then(ints).containsExactly(1, 2, 3);
    }

    @GraphQLClientApi
    interface IntegerApi {
        Integer[] ints(Integer[] in);
    }

    @Test
    void shouldCallIntegerArrayQuery() {
        fixture.returnsData("'ints':[1,2,3]");
        IntegerApi api = fixture.build(IntegerApi.class);

        Integer[] ints = api.ints(new Integer[] { 1, 2, 3 });

        then(fixture.query()).isEqualTo("query ints($in: [Int]) { ints(in: $in) }");
        then(ints).containsExactly(1, 2, 3);
    }

    // -------------------------

    @GraphQLClientApi
    interface PrimitiveLongApi {
        long[] longs(long[] in);
    }

    @Test
    void shouldCallPrimitiveLongArrayQuery() {
        fixture.returnsData("'longs':[1,2,3]");
        PrimitiveLongApi api = fixture.build(PrimitiveLongApi.class);

        long[] longs = api.longs(new long[] { 1, 2, 3 });

        then(fixture.query()).isEqualTo("query longs($in: [BigInteger!]) { longs(in: $in) }");
        then(longs).containsExactly(1, 2, 3);
    }

    @GraphQLClientApi
    interface LongApi {
        Long[] longs(Long[] in);
    }

    @Test
    void shouldCallLongArrayQuery() {
        fixture.returnsData("'longs':[1,2,3]");
        LongApi api = fixture.build(LongApi.class);

        Long[] longs = api.longs(new Long[] { 1L, 2L, 3L });

        then(fixture.query()).isEqualTo("query longs($in: [BigInteger]) { longs(in: $in) }");
        then(longs).containsExactly(1L, 2L, 3L);
    }

    // -------------------------

    @GraphQLClientApi
    interface PrimitiveFloatApi {
        float[] floats(float[] in);
    }

    @Test
    void shouldCallPrimitiveFloatArrayQuery() {
        fixture.returnsData("'floats':[1,2,3]");
        PrimitiveFloatApi api = fixture.build(PrimitiveFloatApi.class);

        float[] floats = api.floats(new float[] { 1, 2, 3 });

        then(fixture.query()).isEqualTo("query floats($in: [Float!]) { floats(in: $in) }");
        then(floats).containsExactly(1, 2, 3);
    }

    @GraphQLClientApi
    interface FloatApi {
        Float[] floats(Float[] in);
    }

    @Test
    void shouldCallFloatArrayQuery() {
        fixture.returnsData("'floats':[1,2,3]");
        FloatApi api = fixture.build(FloatApi.class);

        Float[] floats = api.floats(new Float[] { 1F, 2F, 3F });

        then(fixture.query()).isEqualTo("query floats($in: [Float]) { floats(in: $in) }");
        then(floats).containsExactly(1F, 2F, 3F);
    }

    // -------------------------

    @GraphQLClientApi
    interface PrimitiveDoubleApi {
        double[] doubles(double[] in);
    }

    @Test
    void shouldCallPrimitiveDoubleArrayQuery() {
        fixture.returnsData("'doubles':[1,2,3]");
        PrimitiveDoubleApi api = fixture.build(PrimitiveDoubleApi.class);

        double[] doubles = api.doubles(new double[] { 1, 2, 3 });

        then(fixture.query()).isEqualTo("query doubles($in: [Float!]) { doubles(in: $in) }");
        then(doubles).containsExactly(1, 2, 3);
    }

    @GraphQLClientApi
    interface DoubleApi {
        Double[] doubles(Double[] in);
    }

    @Test
    void shouldCallDoubleArrayQuery() {
        fixture.returnsData("'doubles':[1,2,3]");
        DoubleApi api = fixture.build(DoubleApi.class);

        Double[] doubles = api.doubles(new Double[] { 1D, 2D, 3D });

        then(fixture.query()).isEqualTo("query doubles($in: [Float]) { doubles(in: $in) }");
        then(doubles).containsExactly(1D, 2D, 3D);
    }
}

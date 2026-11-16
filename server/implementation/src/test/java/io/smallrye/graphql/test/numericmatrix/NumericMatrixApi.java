package io.smallrye.graphql.test.numericmatrix;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class NumericMatrixApi {

    /** A float whose widened double differs from its shortest decimal (0.10000000149011612 vs 0.1). */
    public static final float FLOAT_FRACTION = 0.1f;

    @Query
    public byte[] matrixByteArray() {
        return new byte[] { Byte.MIN_VALUE, Byte.MAX_VALUE };
    }

    @Query
    public short[] matrixShortArray() {
        return new short[] { Short.MIN_VALUE, Short.MAX_VALUE };
    }

    @Query
    public int[] matrixIntArray() {
        return new int[] { Integer.MIN_VALUE, Integer.MAX_VALUE };
    }

    @Query
    public long[] matrixLongArray() {
        return new long[] { Long.MIN_VALUE, Long.MAX_VALUE };
    }

    @Query
    public float[] matrixFloatArray() {
        return new float[] { FLOAT_FRACTION, Float.MAX_VALUE };
    }

    @Query
    public Float[] matrixBoxedFloatArray() {
        return new Float[] { FLOAT_FRACTION, Float.MAX_VALUE };
    }

    @Query
    public double[] matrixDoubleArray() {
        return new double[] { 0.1d, Double.MAX_VALUE };
    }

    @Query
    public String matrixPojo(@Name("input") @NonNull MatrixPojo input, @Name("field") @NonNull String field) {
        return fieldValue(input, field);
    }

    @Query
    public String matrixRecord(@Name("input") @NonNull MatrixRecord input, @Name("field") @NonNull String field) {
        return fieldValue(input, field);
    }

    @Query
    public String matrixNestedPojo(@Name("input") @NonNull MatrixPojoEnvelope input,
            @Name("field") @NonNull String field) {
        return fieldValue(input.child, field);
    }

    @Query
    public String matrixNestedRecord(@Name("input") @NonNull MatrixRecordEnvelope input,
            @Name("field") @NonNull String field) {
        return fieldValue(input.child(), field);
    }

    @Query
    public String matrixPojoList(@Name("input") @NonNull List<MatrixPojo> input,
            @Name("field") @NonNull String field) {
        return fieldValue(input.get(0), field);
    }

    @Query
    public String matrixRecordList(@Name("input") @NonNull List<MatrixRecord> input,
            @Name("field") @NonNull String field) {
        return fieldValue(input.get(0), field);
    }

    @Query
    public String matrixPrimitiveByte(@Name("input") byte input) {
        return render(input);
    }

    @Query
    public String matrixBoxedByte(@Name("input") Byte input) {
        return render(input);
    }

    @Query
    public String matrixPrimitiveShort(@Name("input") short input) {
        return render(input);
    }

    @Query
    public String matrixBoxedShort(@Name("input") Short input) {
        return render(input);
    }

    @Query
    public String matrixPrimitiveInt(@Name("input") int input) {
        return render(input);
    }

    @Query
    public String matrixBoxedInt(@Name("input") Integer input) {
        return render(input);
    }

    @Query
    public String matrixPrimitiveLong(@Name("input") long input) {
        return render(input);
    }

    @Query
    public String matrixBoxedLong(@Name("input") Long input) {
        return render(input);
    }

    @Query
    public String matrixPrimitiveFloat(@Name("input") float input) {
        return render(input);
    }

    @Query
    public String matrixBoxedFloat(@Name("input") Float input) {
        return render(input);
    }

    @Query
    public String matrixPrimitiveDouble(@Name("input") double input) {
        return render(input);
    }

    @Query
    public String matrixBoxedDouble(@Name("input") Double input) {
        return render(input);
    }

    @Query
    public String matrixAtomicInteger(@Name("input") AtomicInteger input) {
        return render(input);
    }

    @Query
    public String matrixAtomicLong(@Name("input") AtomicLong input) {
        return render(input);
    }

    @Query
    public String matrixOptionalInt(@Name("input") OptionalInt input) {
        return render(java.util.Objects.requireNonNull(input, "OptionalInt must be empty rather than null"));
    }

    @Query
    public String matrixOptionalLong(@Name("input") OptionalLong input) {
        return render(java.util.Objects.requireNonNull(input, "OptionalLong must be empty rather than null"));
    }

    @Query
    public String matrixOptionalDouble(@Name("input") OptionalDouble input) {
        return render(java.util.Objects.requireNonNull(input, "OptionalDouble must be empty rather than null"));
    }

    @Query
    public String matrixOptionalBoxedByte(@Name("input") Optional<Byte> input) {
        return render(input);
    }

    @Query
    public String matrixOptionalBoxedShort(@Name("input") Optional<Short> input) {
        return render(input);
    }

    @Query
    public String matrixOptionalBoxedInteger(@Name("input") Optional<Integer> input) {
        return render(input);
    }

    @Query
    public String matrixOptionalBoxedLong(@Name("input") Optional<Long> input) {
        return render(input);
    }

    @Query
    public String matrixOptionalBoxedFloat(@Name("input") Optional<Float> input) {
        return render(input);
    }

    @Query
    public String matrixOptionalBoxedDouble(@Name("input") Optional<Double> input) {
        return render(input);
    }

    private static String fieldValue(Object input, String field) {
        try {
            Object value = input.getClass().isRecord() ? input.getClass().getMethod(field).invoke(input)
                    : input.getClass().getField(field).get(input);
            return render(value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(field, e);
        }
    }

    private static String render(Object value) {
        if (value == null) {
            return "empty";
        } else if (value instanceof Optional<?> optional) {
            return optional.map(NumericMatrixApi::render).orElse("empty");
        } else if (value instanceof OptionalInt optional) {
            return optional.isPresent() ? Integer.toString(optional.getAsInt()) : "empty";
        } else if (value instanceof OptionalLong optional) {
            return optional.isPresent() ? Long.toString(optional.getAsLong()) : "empty";
        } else if (value instanceof OptionalDouble optional) {
            return optional.isPresent() ? Double.toString(optional.getAsDouble()) : "empty";
        }
        return value.toString();
    }

    public static class MatrixPojo {
        public byte primitiveByte;
        public Byte boxedByte;
        public short primitiveShort;
        public Short boxedShort;
        public int primitiveInt;
        public Integer boxedInt;
        public long primitiveLong;
        public Long boxedLong;
        public float primitiveFloat;
        public Float boxedFloat;
        public double primitiveDouble;
        public Double boxedDouble;
        public AtomicInteger atomicInteger;
        public AtomicLong atomicLong;
        public OptionalInt optionalInt = OptionalInt.empty();
        public OptionalLong optionalLong = OptionalLong.empty();
        public OptionalDouble optionalDouble = OptionalDouble.empty();
        public Optional<Byte> optionalBoxedByte = Optional.empty();
        public Optional<Short> optionalBoxedShort = Optional.empty();
        public Optional<Integer> optionalBoxedInteger = Optional.empty();
        public Optional<Long> optionalBoxedLong = Optional.empty();
        public Optional<Float> optionalBoxedFloat = Optional.empty();
        public Optional<Double> optionalBoxedDouble = Optional.empty();
    }

    public record MatrixRecord(byte primitiveByte, Byte boxedByte, short primitiveShort, Short boxedShort,
            int primitiveInt, Integer boxedInt, long primitiveLong, Long boxedLong,
            float primitiveFloat, Float boxedFloat, double primitiveDouble, Double boxedDouble,
            AtomicInteger atomicInteger, AtomicLong atomicLong,
            OptionalInt optionalInt, OptionalLong optionalLong, OptionalDouble optionalDouble,
            Optional<Byte> optionalBoxedByte, Optional<Short> optionalBoxedShort,
            Optional<Integer> optionalBoxedInteger, Optional<Long> optionalBoxedLong,
            Optional<Float> optionalBoxedFloat, Optional<Double> optionalBoxedDouble) {
    }

    public static class MatrixPojoEnvelope {
        public MatrixPojo child;
    }

    public record MatrixRecordEnvelope(MatrixRecord child) {
    }
}

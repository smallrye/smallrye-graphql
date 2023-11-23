package io.smallrye.graphql.tests.customscalars;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.microprofile.graphql.DefaultValue;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.NonNull;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class SomeApi {
    public static class ObjNullable {
        public BigDecimalString scalar;
    }

    public static class FObjNullable {
        public TwiceTheFloat fScalar;
    }

    public static class ObjOfScalars {
        public TwiceTheFloat fScalar;
        public BigDecimalString sScalar;
    }

    @Query
    public String inAsScalarNullable(BigDecimalString scalar) {
        return null == scalar ? null : scalar.toString();
    }

    @Query
    public String inAsScalarNullableDefaultNonNull(@DefaultValue("1234567.89") BigDecimalString scalar) {
        return null == scalar ? null : scalar.toString();
    }

    @Query
    public String inAsScalarNullableDefaultNull(@DefaultValue() BigDecimalString scalar) {
        return null == scalar ? null : scalar.toString();
    }

    @Query
    public String inAsScalarRequiredDefault(@NonNull @DefaultValue("1234567.89") BigDecimalString scalar) {
        return null == scalar ? null : scalar.toString();
    }

    @Query
    public String inAsScalarRequired(@NonNull BigDecimalString scalar) {
        return null == scalar ? null : scalar.toString();
    }

    @Query
    public List<String> inAsScalarListNullable(List<BigDecimalString> scalars) {
        return null == scalars ? null : scalars.stream().map(s -> null == s ? null : s.toString()).collect(Collectors.toList());
    }

    @Query
    public List<String> inAsScalarListRequired(List<@NonNull BigDecimalString> scalars) {
        return null == scalars ? null : scalars.stream().map(Objects::toString).collect(Collectors.toList());
    }

    @Query
    public String inAsFieldNullable(ObjNullable input) {
        return null == input ? null : null == input.scalar ? null : input.scalar.toString();
    }

    @Query
    public BigDecimal inAsFScalarNullable(TwiceTheFloat fScalar) {
        return null == fScalar ? null : fScalar.floatValue().setScale(1, RoundingMode.HALF_EVEN);
    }

    @Query
    public BigDecimal inAsFFieldNullable(FObjNullable input) {
        return null == input ? null
                : null == input.fScalar ? null
                  : input.fScalar.floatValue().setScale(1, RoundingMode.HALF_EVEN);
    }

    @Query
    public TwiceTheFloat outputFloat() {
        return new TwiceTheFloat(BigDecimal.valueOf(10));
    }

    @Query
    public ObjOfScalars outputScalars() {
        ObjOfScalars oScalars =  new ObjOfScalars();
        oScalars.fScalar = new TwiceTheFloat(BigDecimal.valueOf(30));
        oScalars.sScalar = new BigDecimalString("98765.56789");
        return oScalars;
    }

}

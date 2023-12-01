package io.smallrye.graphql.tests.customscalars;

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
    return null == scalars ? null :
        scalars.stream().map(s -> null == s ? null : s.toString()).collect(Collectors.toList());
  }

  @Query
  public List<String> inAsScalarListRequired(List<@NonNull BigDecimalString> scalars) {
    return null == scalars ? null :
        scalars.stream().map(Objects::toString).collect(Collectors.toList());
  }


  @Query
  public String inAsFieldNullable(ObjNullable input) {
    return null == input ? null : null == input.scalar ? null : input.scalar.toString();
  }

}

package io.smallrye.graphql.tests.customscalars;

import io.smallrye.graphql.api.CustomScalar;
import io.smallrye.graphql.scalar.custom.CustomStringScalar;
import java.math.BigDecimal;

/**
 * An alternative BigDecimal scalar that serializes to a GraphQL String instead of a Float.
 * <p>
 * TODO bdupras
 *   - I'd like to remove the requirement to implement the CustomStringScalar interface. It
 *     seems like a weird API to require _both_ an annotation and an interface or an
 *     abstract base class. See notes in JsonBCreator about why the interface (or base class)
 *     is currently necessary to make things work.
 */
@CustomScalar("BigDecimalString")
public class BigDecimalString implements CustomStringScalar {

  private final BigDecimal value;

  public BigDecimalString(String stringValue) {
    this.value = stringValue == null ? null : new BigDecimal(stringValue);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) || value.equals(obj);
  }

  @Override
  public String toString() {
    return value == null ? null : value.toPlainString();
  }

}

package io.smallrye.graphql.scalar.federation;

import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.scalar.AbstractScalar;

/**
 * Scalar for {@link FieldSet}.
 */
public class FieldSetScalar extends AbstractScalar {

    public FieldSetScalar() {

        super("FieldSet", new FieldSetCoercing(), FieldSet.class);
    }
}

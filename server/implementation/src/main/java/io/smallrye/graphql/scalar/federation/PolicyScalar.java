package io.smallrye.graphql.scalar.federation;

import io.smallrye.graphql.api.federation.policy.PolicyItem;
import io.smallrye.graphql.scalar.AbstractScalar;

/**
 * Scalar for {@link PolicyItem}.
 */
public class PolicyScalar extends AbstractScalar {

    public PolicyScalar() {

        super("Policy", new PolicyCoercing(), PolicyItem.class);
    }
}

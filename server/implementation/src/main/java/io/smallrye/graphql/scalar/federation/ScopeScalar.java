package io.smallrye.graphql.scalar.federation;

import io.smallrye.graphql.api.federation.requiresscopes.ScopeItem;
import io.smallrye.graphql.scalar.AbstractScalar;

/**
 * Scalar for {@link ScopeItem}.
 */
public class ScopeScalar extends AbstractScalar {

    public ScopeScalar() {

        super("Scope", new ScopeCoercing(), ScopeItem.class);
    }
}
